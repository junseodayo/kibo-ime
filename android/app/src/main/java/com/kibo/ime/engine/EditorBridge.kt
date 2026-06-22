package com.kibo.ime.engine

/**
 * The surface an [InputEngine] writes to. Implemented over an Android
 * `InputConnection` by the IME service, but kept as a narrow interface so the
 * engines (especially the Korean automaton) are unit-testable without Android.
 */
interface EditorBridge {
    /** Replace the current composing region with [text] (still being composed). */
    fun setComposing(text: CharSequence)

    /** Finalize whatever is currently in the composing region as committed text. */
    fun finishComposing()

    /** Commit [text] immediately (no composing region involved). */
    fun commitText(text: CharSequence)

    /** Delete [count] characters before the cursor (committed text). */
    fun deleteBefore(count: Int)

    /** Delete one whitespace-delimited word before the cursor (spec §3 long backspace). */
    fun deleteWordBefore()
}
