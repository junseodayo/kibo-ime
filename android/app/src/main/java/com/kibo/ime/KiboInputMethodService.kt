package com.kibo.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodService
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.kibo.ime.clipboard.ClipboardRepository
import com.kibo.ime.engine.EditorBridge
import com.kibo.ime.engine.JapaneseConverterFactory
import com.kibo.ime.engine.JapaneseEngine
import com.kibo.ime.engine.Language
import com.kibo.ime.input.Fullwidth
import com.kibo.ime.input.LanguageController
import com.kibo.ime.prefs.ClipboardSettings
import com.kibo.ime.prefs.KeyBinding
import com.kibo.ime.prefs.SettingsRepository
import com.kibo.ime.ui.ime.ImePanel
import com.kibo.ime.ui.ime.ImeActions
import com.kibo.ime.ui.ime.ImeRoot
import com.kibo.ime.ui.ime.ImeUiState
import com.kibo.ime.ui.ime.SoftKey
import com.kibo.ime.ui.settings.SettingsActivity
import com.kibo.ime.ui.theme.KiboTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Kibo IME (spec §1). An [InputMethodService] that intercepts **hardware** key
 * events ([onKeyDown]/[onKeyUp]) and routes them through a per-language engine,
 * while rendering only its software toolbar / candidate strip / panels with Compose.
 */
class KiboInputMethodService : InputMethodService(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    // --- Compose hosting plumbing (IME is not a LifecycleOwner by default) ----
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val viewModelStoreField = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = viewModelStoreField
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    // Built in onCreate so the Japanese engine can pick up a Mozc converter (or stub).
    private lateinit var controller: LanguageController
    private val uiState = ImeUiState()

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var clipRepo: ClipboardRepository

    private var switchKey: KeyBinding = KeyBinding.DEFAULT
    private var appLangMasterOn = false
    private var appLangFor: (String?) -> Language? = { null }
    private var clipSettings = ClipboardSettings()
    private var lastUsedLanguage = Language.KOREAN

    private var passwordField = false
    private var longBackspaceHandled = false

    private val clipboardManager by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (passwordField) return@OnPrimaryClipChangedListener
        val text = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        if (!text.isNullOrBlank()) scope.launch { clipRepo.record(text, clipSettings) }
    }

    // --- The editor surface engines write to --------------------------------
    private val bridge: EditorBridge = object : EditorBridge {
        override fun setComposing(text: CharSequence) {
            currentInputConnection?.setComposingText(text, 1)
        }
        override fun finishComposing() {
            currentInputConnection?.finishComposingText()
        }
        override fun commitText(text: CharSequence) {
            currentInputConnection?.commitText(text, 1)
        }
        override fun deleteBefore(count: Int) {
            currentInputConnection?.deleteSurroundingText(count, 0)
        }
        override fun deleteWordBefore() {
            val ic = currentInputConnection ?: return
            val before = ic.getTextBeforeCursor(64, 0) ?: return
            if (before.isEmpty()) return
            var i = before.length
            while (i > 0 && before[i - 1].isWhitespace()) i--
            while (i > 0 && !before[i - 1].isWhitespace()) i--
            val count = (before.length - i).coerceAtLeast(1)
            ic.deleteSurroundingText(count, 0)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        settingsRepo = SettingsRepository(this)
        clipRepo = ClipboardRepository(this)
        controller = LanguageController(
            japanese = JapaneseEngine(JapaneseConverterFactory.create(this)),
        )
        observeSettings()
        observeClipboard()
    }

    private fun observeSettings() {
        settingsRepo.settings.onEach { s ->
            controller.setOrder(s.languageOrder)
            switchKey = s.switchKey
            appLangMasterOn = s.appLangMasterOn
            appLangFor = { pkg -> s.appLangMappings.firstOrNull { it.packageName == pkg }?.language }
            clipSettings = s.clipboard
            uiState.accentColor = s.accentColor
        }.launchIn(scope)
    }

    private fun observeClipboard() {
        clipRepo.history.onEach { uiState.clipHistory = it }.launchIn(scope)
        clipRepo.presets.onEach { uiState.clipPresets = it }.launchIn(scope)
    }

    // Always show our toolbar even when a hardware keyboard is connected.
    override fun onEvaluateInputViewShown() = true
    override fun onEvaluateFullscreenMode() = false

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@KiboInputMethodService)
            setViewTreeViewModelStoreOwner(this@KiboInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@KiboInputMethodService)
            setContent {
                val accent = uiState.accentColor?.let { Color(it) }
                KiboTheme(accentOverride = accent) {
                    ImeRoot(uiState, actions)
                }
            }
        }
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        return composeView
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        passwordField = info != null && isPasswordField(info)

        // App-specific default language (spec §11).
        val targetLang = if (appLangMasterOn) {
            appLangFor(info?.packageName) ?: lastUsedLanguage
        } else {
            lastUsedLanguage
        }
        controller.forceLanguage(targetLang)
        syncModeState()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        uiState.panel = ImePanel.NONE
        longBackspaceHandled = false
        runCatching { clipboardManager.addPrimaryClipChangedListener(clipListener) }
        syncModeState()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        runCatching { clipboardManager.removePrimaryClipChangedListener(clipListener) }
        controller.engine.reset()
        uiState.panel = ImePanel.NONE
        uiState.candidates = null
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStoreField.clear()
        scope.cancel()
        super.onDestroy()
    }

    // ------------------------------------------------------------------------
    // Hardware key handling (spec §1, §2, §6)
    // ------------------------------------------------------------------------

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (handleKeyDown(keyCode, event)) return true
        // Not consumed: commit any composition so passthrough chars land cleanly.
        if (controller.engine.isComposing) controller.engine.finishComposition(bridge)
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        updateMetaIndicators(event)
        if (keyCode == KeyEvent.KEYCODE_DEL) longBackspaceHandled = false
        // Consume the up-event of the switch combo so the host never sees it.
        if (matchesSwitchKey(keyCode, event)) return true
        return super.onKeyUp(keyCode, event)
    }

    private fun handleKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        updateMetaIndicators(event)

        // 1) Language switch key (spec §2) — must win before normal space.
        if (matchesSwitchKey(keyCode, event)) {
            if (event.repeatCount == 0) cycleLanguage()
            return true
        }

        // 2) Editing keys
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> { handleBackspace(event); return true }
            KeyEvent.KEYCODE_SPACE -> {
                val consumed = controller.engine.onSpace(bridge)
                refreshCandidates()
                return consumed
            }
            KeyEvent.KEYCODE_ENTER -> {
                val consumed = controller.engine.onEnter(bridge)
                refreshCandidates()
                return consumed
            }
        }

        // 3) Printable character keys
        val baseChar = event.getUnicodeChar(0)
        if (baseChar == 0) return false  // arrows, ctrl, etc. — let the host handle

        // 3a) alt/sym layer (spec §6): commit composition first, then insert the
        //     OS-resolved symbol; never feed it to the language engine.
        if (isSymbolLayer(event)) {
            val uni = event.getUnicodeChar(event.metaState)
            if (uni != 0) {
                controller.engine.finishComposition(bridge)
                var ch = uni.toChar()
                if (controller.current == Language.JAPANESE) ch = Fullwidth.toFullwidth(ch)
                bridge.commitText(ch.toString())
                refreshCandidates()
                return true
            }
            return false
        }

        // 3b) normal letter → current language engine
        return when (controller.current) {
            Language.KOREAN -> {
                val shifted = event.metaState and KeyEvent.META_SHIFT_ON != 0
                val ch = if (shifted) baseChar.toChar().uppercaseChar() else baseChar.toChar()
                feedChar(ch)
            }
            Language.JAPANESE -> feedChar(baseChar.toChar())
            Language.ENGLISH -> false  // true passthrough (spec §4)
        }
    }

    private fun feedChar(ch: Char): Boolean {
        val consumed = controller.engine.onCharacter(ch, bridge)
        refreshCandidates()
        return consumed
    }

    private fun handleBackspace(event: KeyEvent) {
        // First press = jamo/char delete; holding = word delete (spec §3).
        if (event.repeatCount == 0) {
            if (!controller.engine.onBackspace(longPress = false, bridge)) {
                currentInputConnection?.deleteSurroundingText(1, 0)
            }
            refreshCandidates()
        } else if (!longBackspaceHandled && event.repeatCount >= 1) {
            longBackspaceHandled = true
            controller.engine.onBackspace(longPress = true, bridge)
            refreshCandidates()
        }
    }

    private fun cycleLanguage() {
        val newLang = controller.cycle(bridge)
        lastUsedLanguage = newLang
        syncModeState()
    }

    private fun switchLanguageTo(lang: Language) {
        controller.switchTo(lang, bridge)
        lastUsedLanguage = lang
        syncModeState()
    }

    private fun matchesSwitchKey(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode != switchKey.keyCode) return false
        if (switchKey.metaState == 0) return false  // a modifier is required (spec §2)
        return event.metaState and switchKey.metaState == switchKey.metaState
    }

    private fun isSymbolLayer(event: KeyEvent): Boolean =
        event.metaState and (KeyEvent.META_ALT_ON or KeyEvent.META_SYM_ON) != 0

    private fun updateMetaIndicators(event: KeyEvent) {
        uiState.caps = event.metaState and KeyEvent.META_CAPS_LOCK_ON != 0
        uiState.symLayer = isSymbolLayer(event)
    }

    private fun refreshCandidates() {
        uiState.candidates = controller.engine.candidates()
    }

    private fun syncModeState() {
        uiState.language = controller.current
        refreshCandidates()
    }

    private fun isPasswordField(info: EditorInfo): Boolean {
        val klass = info.inputType and InputType.TYPE_MASK_CLASS
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val textPw = klass == InputType.TYPE_CLASS_TEXT &&
            (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
        val numberPw = klass == InputType.TYPE_CLASS_NUMBER &&
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        return textPw || numberPw
    }

    // ------------------------------------------------------------------------
    // On-screen surface callbacks (spec §8, §9, §10)
    // ------------------------------------------------------------------------

    private val actions = object : ImeActions {
        override fun toggleOsk() { uiState.oskVisible = !uiState.oskVisible }

        override fun openPanel(panel: ImePanel) { uiState.panel = panel }
        override fun closePanel() { uiState.panel = ImePanel.NONE }

        override fun insert(text: String) {
            controller.engine.finishComposition(bridge)
            bridge.commitText(text)
            refreshCandidates()
        }

        override fun onSoftKey(key: SoftKey) {
            when (key) {
                is SoftKey.Char -> {
                    val ch = key.text.firstOrNull() ?: return
                    if (!controller.engine.onCharacter(ch, bridge)) {
                        controller.engine.finishComposition(bridge)
                        bridge.commitText(key.text)
                    }
                    refreshCandidates()
                }
                SoftKey.Backspace -> {
                    if (!controller.engine.onBackspace(false, bridge)) {
                        currentInputConnection?.deleteSurroundingText(1, 0)
                    }
                    refreshCandidates()
                }
                SoftKey.Space -> { if (!controller.engine.onSpace(bridge)) bridge.commitText(" "); refreshCandidates() }
                SoftKey.Enter -> { if (!controller.engine.onEnter(bridge)) bridge.commitText("\n"); refreshCandidates() }
                SoftKey.Shift -> uiState.caps = !uiState.caps
                SoftKey.SymToggle -> uiState.symLayer = !uiState.symLayer
            }
        }

        override fun pickCandidate(index: Int) {
            controller.engine.selectCandidate(index, bridge)
            refreshCandidates()
        }

        override fun openSettings() {
            startActivity(
                Intent(this@KiboInputMethodService, SettingsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        override fun switchLanguage(language: Language) = switchLanguageTo(language)
    }
}
