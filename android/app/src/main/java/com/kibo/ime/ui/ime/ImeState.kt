package com.kibo.ime.ui.ime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kibo.ime.engine.CandidateState
import com.kibo.ime.engine.Language
import com.kibo.ime.prefs.ClipItem
import com.kibo.ime.prefs.PresetItem

/** Which slide-up panel (if any) is open over the toolbar (spec §9, §10). */
enum class ImePanel { NONE, EMOJI, SYMBOL, CLIPBOARD }

/** Observable UI state for the on-screen IME surfaces, driven by the service. */
class ImeUiState {
    var language by mutableStateOf(Language.KOREAN)
    var caps by mutableStateOf(false)
    var symLayer by mutableStateOf(false)
    var candidates by mutableStateOf<CandidateState?>(null)
    var panel by mutableStateOf(ImePanel.NONE)
    var oskVisible by mutableStateOf(false)
    var accentColor by mutableStateOf<Int?>(null)
    var clipHistory by mutableStateOf<List<ClipItem>>(emptyList())
    var clipPresets by mutableStateOf<List<PresetItem>>(emptyList())
}

/** A soft key on the on-screen fallback keyboard (spec §10). */
sealed interface SoftKey {
    data class Char(val text: String) : SoftKey
    data object Backspace : SoftKey
    data object Space : SoftKey
    data object Enter : SoftKey
    data object Shift : SoftKey
    data object SymToggle : SoftKey
}

/** Callbacks the on-screen surfaces invoke back into the service. */
interface ImeActions {
    fun toggleOsk()
    fun openPanel(panel: ImePanel)
    fun closePanel()
    /** Insert literal text now (emoji/symbol glyph, pasted clip/preset). */
    fun insert(text: String)
    fun onSoftKey(key: SoftKey)
    fun pickCandidate(index: Int)
    fun openSettings()
    fun switchLanguage(language: Language)
}
