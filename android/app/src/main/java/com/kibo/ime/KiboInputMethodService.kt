package com.kibo.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.inputmethodservice.InputMethodService
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
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

    // Hold-to-delete-words throttle: delete one word per this interval rather than once
    // per OS auto-repeat (which clears everything almost instantly).
    private val wordDeleteIntervalMs = 200L
    private var lastWordDeleteTime = 0L

    // --- Tap / sticky modifier state (one-shot + double-tap-lock) -----------
    private enum class ModState { OFF, ONESHOT, LOCKED }
    private var altLayer = ModState.OFF
    private var shiftMod = ModState.OFF
    // Tracks a modifier key currently held alone, so onKeyUp can tell a *tap*
    // (press + release with nothing in between) from a normal hold-combo.
    private var heldModifierKey = 0
    private var heldModifierIsTap = false

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
        // The bottom strip behind the system nav buttons takes our accent (point) color;
        // the nav-bar icon contrast is picked from the accent's brightness so they stay visible.
        val accentArgb = uiState.accentColor ?: 0xFFC8F03C.toInt()  // default = Lime400
        val accentIsLight = Color(accentArgb).luminance() > 0.5f

        // Compose installs its recomposer by walking UP from the IME window's
        // content root (android:id/parentPanel), not from our ComposeView. So the
        // ViewTree owners must also be set on an ancestor — the window decorView —
        // or it throws "ViewTreeLifecycleOwner not found". (Setting them on the
        // ComposeView alone is not enough for a non-Activity window.)
        window?.window?.let { w ->
            w.decorView.setViewTreeLifecycleOwner(this@KiboInputMethodService)
            w.decorView.setViewTreeViewModelStoreOwner(this@KiboInputMethodService)
            w.decorView.setViewTreeSavedStateRegistryOwner(this@KiboInputMethodService)
            // We can't hide the system IME nav bar on this device, so keep it — just make
            // it transparent with no contrast scrim, so our toolbar surface fills behind
            // it cleanly. The toolbar is lifted above it via the inset padding below.
            w.navigationBarColor = android.graphics.Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                w.isNavigationBarContrastEnforced = false
            }
            // The bottom strip shows our accent color, so set the nav-bar icon contrast
            // from the accent's brightness (dark icons on a light accent, light on dark).
            WindowCompat.getInsetsController(w, w.decorView)
                .isAppearanceLightNavigationBars = accentIsLight
        }

        // Height of the accent strip behind the system nav buttons (nav-bar height + a bit
        // extra). Drawn inside Compose (passed to ImeRoot) so it recolors live when the
        // accent changes — a one-time View.setBackgroundColor would not.
        val density = resources.displayMetrics.density
        val navStripDp = (systemNavBarHeightPx() / density + 12f).dp

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@KiboInputMethodService)
            setViewTreeViewModelStoreOwner(this@KiboInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@KiboInputMethodService)
            setContent {
                val accent = uiState.accentColor?.let { Color(it) }
                KiboTheme(accentOverride = accent) {
                    ImeRoot(uiState, actions, navStripDp)
                }
            }
        }
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        return composeView
    }

    /** Match the system nav-bar icon contrast to the current accent (re-applied when it changes). */
    private fun applyNavBarIconContrast() {
        val accentArgb = uiState.accentColor ?: 0xFFC8F03C.toInt()
        window?.window?.let { w ->
            WindowCompat.getInsetsController(w, w.decorView)
                .isAppearanceLightNavigationBars = Color(accentArgb).luminance() > 0.5f
        }
    }

    /** System navigation-bar height (px) from platform resources; 0 if not present. */
    private fun systemNavBarHeightPx(): Int {
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        passwordField = info != null && isPasswordField(info)

        // Numeric fields (OTP / phone / PIN): auto-lock the alt layer so the digits
        // printed on the keys type directly without holding alt (spec §6).
        altLayer = if (info != null && isNumericField(info)) ModState.LOCKED else ModState.OFF
        shiftMod = ModState.OFF
        uiState.symLayer = altLayer != ModState.OFF
        uiState.caps = false

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
        runCatching { clipboardManager.addPrimaryClipChangedListener(clipListener) }
        applyNavBarIconContrast()  // refresh in case the accent changed in settings
        syncModeState()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        runCatching { clipboardManager.removePrimaryClipChangedListener(clipListener) }
        controller.engine.reset()
        altLayer = ModState.OFF
        shiftMod = ModState.OFF
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
        // (Modifier keys must NOT finish the composition — they may precede more input.)
        if (!isModifierKeyCode(keyCode) && controller.engine.isComposing) {
            controller.engine.finishComposition(bridge)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        // A modifier released with nothing pressed in between = a tap: cycle its
        // state OFF → one-shot → locked → OFF (spec §6 tap/sticky modifiers).
        if (isModifierKeyCode(keyCode) && keyCode == heldModifierKey) {
            val wasTap = heldModifierIsTap
            heldModifierKey = 0
            heldModifierIsTap = false
            if (wasTap) onModifierTap(keyCode)
        }
        updateMetaIndicators(event)
        // Consume the up-event of the switch combo so the host never sees it.
        if (matchesSwitchKey(keyCode, event)) return true
        return super.onKeyUp(keyCode, event)
    }

    private fun handleKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        updateMetaIndicators(event)

        // Track modifier keys so onKeyUp can detect a tap vs. a hold-combo.
        if (isModifierKeyCode(keyCode)) {
            if (event.repeatCount == 0) {
                heldModifierKey = keyCode
                heldModifierIsTap = true
            }
            return false  // let the OS track real meta-state for hold-combos
        }
        // Any non-modifier key means a held modifier is part of a hold-combo, not a tap.
        heldModifierIsTap = false

        // 1) Language switch key (spec §2) — must win before normal space/enter.
        if (matchesSwitchKey(keyCode, event)) {
            if (event.repeatCount == 0) cycleLanguage()
            return true
        }

        // Effective modifiers for THIS key = physically held (OS meta) OR our tap/lock state.
        val altActive = isSymbolLayer(event) || altLayer != ModState.OFF
        val shiftHeld = event.metaState and KeyEvent.META_SHIFT_ON != 0
        val shiftActive = shiftHeld || shiftMod != ModState.OFF
        val oneShotAlt = altLayer == ModState.ONESHOT
        val oneShotShift = shiftMod == ModState.ONESHOT

        // 2) Editing keys
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> { handleBackspace(event); return true }
            KeyEvent.KEYCODE_SPACE -> {
                val consumed = controller.engine.onSpace(bridge)
                clearOneShotModifiers()
                refreshCandidates()
                return consumed
            }
            KeyEvent.KEYCODE_ENTER -> {
                val consumed = controller.engine.onEnter(bridge)
                clearOneShotModifiers()
                refreshCandidates()
                return consumed
            }
        }

        // 3) Printable character keys
        val baseChar = event.getUnicodeChar(0)
        if (baseChar == 0) return false  // arrows, ctrl, etc. — let the host handle

        // 3a) alt/sym layer (spec §6): commit composition first, then insert the
        //     OS-resolved symbol; never feed it to the language engine.
        if (altActive) {
            // Synthesize the alt meta so the Key Character Map resolves the printed
            // symbol/digit even when alt is tapped/locked rather than physically held.
            val meta = event.metaState or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
            val uni = event.getUnicodeChar(meta)
            if (uni != 0) {
                val ch = uni.toChar()
                // In 日本語, the long-vowel '-' (→ ー) and ' belong to the kana reading, so
                // feed them to the engine to extend the composition instead of breaking it (§5).
                if (controller.current == Language.JAPANESE && (ch == '-' || ch == '\'')) {
                    val consumed = controller.engine.onCharacter(ch, bridge)
                    if (oneShotAlt) clearOneShotModifiers()
                    refreshCandidates()
                    return consumed
                }
                controller.engine.finishComposition(bridge)
                val out = if (controller.current == Language.JAPANESE) Fullwidth.toFullwidth(ch) else ch
                bridge.commitText(out.toString())
                if (oneShotAlt) clearOneShotModifiers()
                refreshCandidates()
                return true
            }
            return false
        }

        // 3b) normal letter → current language engine
        val consumed = when (controller.current) {
            Language.KOREAN -> {
                val ch = if (shiftActive) baseChar.toChar().uppercaseChar() else baseChar.toChar()
                feedChar(ch)
            }
            Language.JAPANESE -> feedChar(baseChar.toChar())
            Language.ENGLISH -> {
                // Hold-shift is handled by the OS passthrough; a tapped/locked shift
                // is not in the OS meta-state, so commit the upper-case letter ourselves.
                if (shiftMod != ModState.OFF && !shiftHeld) {
                    controller.engine.finishComposition(bridge)
                    bridge.commitText(baseChar.toChar().uppercaseChar().toString())
                    true
                } else {
                    false  // true passthrough (spec §4)
                }
            }
        }
        if (oneShotShift) clearOneShotModifiers()
        return consumed
    }

    private fun feedChar(ch: Char): Boolean {
        val consumed = controller.engine.onCharacter(ch, bridge)
        refreshCandidates()
        return consumed
    }

    private fun handleBackspace(event: KeyEvent) {
        // First press = jamo/char delete; while held, delete one word per
        // wordDeleteIntervalMs so a long hold clears back steadily, not instantly (spec §3).
        if (event.repeatCount == 0) {
            if (!controller.engine.onBackspace(longPress = false, bridge)) {
                currentInputConnection?.deleteSurroundingText(1, 0)
            }
            lastWordDeleteTime = event.eventTime
            refreshCandidates()
        } else if (event.eventTime - lastWordDeleteTime >= wordDeleteIntervalMs) {
            lastWordDeleteTime = event.eventTime
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

    private fun isModifierKeyCode(keyCode: Int): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT,
        KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT,
        KeyEvent.KEYCODE_SYM -> true
        else -> false
    }

    private fun isShiftKeyCode(keyCode: Int): Boolean =
        keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT

    /** Tap cycles a modifier: OFF → one-shot (next key only) → locked → OFF. */
    private fun onModifierTap(keyCode: Int) {
        fun next(s: ModState) = when (s) {
            ModState.OFF -> ModState.ONESHOT
            ModState.ONESHOT -> ModState.LOCKED
            ModState.LOCKED -> ModState.OFF
        }
        if (isShiftKeyCode(keyCode)) shiftMod = next(shiftMod) else altLayer = next(altLayer)
    }

    /** Release one-shot modifiers after they've applied to one key; keep locked ones. */
    private fun clearOneShotModifiers() {
        if (altLayer == ModState.ONESHOT) altLayer = ModState.OFF
        if (shiftMod == ModState.ONESHOT) shiftMod = ModState.OFF
        uiState.symLayer = altLayer != ModState.OFF
        uiState.caps = shiftMod != ModState.OFF
    }

    private fun updateMetaIndicators(event: KeyEvent) {
        uiState.caps = event.metaState and KeyEvent.META_CAPS_LOCK_ON != 0 || shiftMod != ModState.OFF
        uiState.symLayer = isSymbolLayer(event) || altLayer != ModState.OFF
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

    /** Number / phone / date-time fields — used to auto-engage the numeric layer (spec §6). */
    private fun isNumericField(info: EditorInfo): Boolean =
        when (info.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> true
            else -> false
        }

    // ------------------------------------------------------------------------
    // On-screen surface callbacks (spec §8, §9, §10)
    // ------------------------------------------------------------------------

    private val actions = object : ImeActions {
        override fun toggleOsk() {
            uiState.oskVisible = !uiState.oskVisible
            // Opening the on-screen keyboard closes any open panel (one surface at a time).
            if (uiState.oskVisible) uiState.panel = ImePanel.NONE
        }

        override fun openPanel(panel: ImePanel) {
            // Tapping the active panel's button closes it; any panel deactivates the OSK.
            uiState.panel = if (uiState.panel == panel) ImePanel.NONE else panel
            uiState.oskVisible = false
        }
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
