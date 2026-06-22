package com.kibo.ime.engine

/**
 * English engine (spec §4): pure passthrough with Shift / Caps Lock,
 * **no autocomplete, no autocorrect**.
 *
 * Returns `false` from key handlers so the IME service performs default key
 * dispatch — the OS inserts the character (already case-resolved via the Key
 * Character Map), giving true passthrough with zero correction logic.
 */
class EnglishEngine : InputEngine {
    override val language = Language.ENGLISH
    override val isComposing = false

    override fun onCharacter(ch: Char, bridge: EditorBridge) = false
    override fun onBackspace(longPress: Boolean, bridge: EditorBridge): Boolean {
        if (longPress) { bridge.deleteWordBefore(); return true }
        return false
    }
    override fun onSpace(bridge: EditorBridge) = false
    override fun onEnter(bridge: EditorBridge) = false
    override fun finishComposition(bridge: EditorBridge) {}
    override fun reset() {}
}
