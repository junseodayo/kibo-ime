package com.kibo.ime.engine

/**
 * A per-language input engine (spec §3 한국어 / §4 영어 / §5 일본어).
 *
 * The dispatcher feeds already-resolved character keys and editing keys; the
 * engine mutates the editor through an [EditorBridge]. A method returns `true`
 * when it consumed the event; `false` lets the IME service fall back to default
 * key handling (e.g. true passthrough for English, inserting a literal space).
 */
interface InputEngine {
    val language: Language

    /** True while an in-progress composition exists (e.g. a half-built 한글 syllable). */
    val isComposing: Boolean

    /**
     * A normal letter key. [ch] is the character to compose (for 한글 this is the
     * Latin key letter so the engine can apply its own 두벌식 map; caps already
     * applied for the case-sensitive double consonants). Returns true if consumed.
     */
    fun onCharacter(ch: Char, bridge: EditorBridge): Boolean

    /** Backspace. [longPress] true ⇒ word-unit delete (spec §3). Returns true if consumed. */
    fun onBackspace(longPress: Boolean, bridge: EditorBridge): Boolean

    /** Space. For 日本語 this advances the candidate (spec §5). Returns true if consumed. */
    fun onSpace(bridge: EditorBridge): Boolean

    /** Enter / confirm. For 日本語 this confirms the candidate (spec §5). Returns true if consumed. */
    fun onEnter(bridge: EditorBridge): Boolean

    /**
     * Commit any in-progress composition right now — called before inserting an
     * alt/sym symbol (spec §6), before a language switch (§2), or on focus loss.
     */
    fun finishComposition(bridge: EditorBridge)

    /** Drop all composition state without committing (e.g. editor reset). */
    fun reset()

    /** Candidate UI state for the toolbar candidate strip; null when not converting. */
    fun candidates(): CandidateState? = null

    /** Touch-select and confirm candidate [index] (spec §5 후보 조작 터치 위주). */
    fun selectCandidate(index: Int, bridge: EditorBridge) {}
}

/** Japanese conversion candidates shown above the toolbar (spec §5). */
data class CandidateState(
    val reading: String,
    val candidates: List<String>,
    val activeIndex: Int,
)
