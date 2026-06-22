package com.kibo.ime.engine

/**
 * 두벌식 표준 한글 오토마타 (spec §3).
 *
 * The composing region always holds exactly the current in-progress syllable.
 * When a syllable finalizes (the next jamo cannot attach, or 도깨비불 moves a
 * 받침 onto a new syllable), the old syllable is baked via [EditorBridge.finishComposing]
 * and a fresh composing region starts.
 *
 * Backspace:
 *  - short press = remove one jamo from the composing syllable;
 *  - long press  = word-unit delete of committed text (handled by the bridge).
 */
class KoreanEngine : InputEngine {

    override val language = Language.KOREAN

    // Current syllable state, in compatibility jamo. null = empty slot.
    private var cho: Char? = null
    private var jung: Char? = null
    private var jong: Char? = null

    override val isComposing: Boolean
        get() = cho != null || jung != null || jong != null

    private fun clear() { cho = null; jung = null; jong = null }

    /** The display text for the current partial syllable. */
    private fun compose(): String = when {
        cho != null && jung != null -> Hangul.compose(cho!!, jung!!, jong).toString()
        cho != null -> cho.toString()          // lone leading consonant (e.g. "ㄱ")
        jung != null -> jung.toString()         // lone vowel (e.g. "ㅏ")
        else -> ""
    }

    /** Bake the current syllable (as it stands now), then begin a new one via [init]. */
    private fun rollover(bridge: EditorBridge, init: () -> Unit) {
        // Sync the composing region to the *current* state first — callers may have
        // reduced it (도깨비불) before rolling over, and finishComposing bakes whatever
        // text the region currently holds.
        bridge.setComposing(compose())
        bridge.finishComposing()
        clear()
        init()
        bridge.setComposing(compose())
    }

    override fun onCharacter(ch: Char, bridge: EditorBridge): Boolean {
        val jamo = Hangul.jamoFor(ch) ?: return false  // not a 두벌식 letter key
        if (Hangul.isVowel(jamo)) handleVowel(jamo, bridge) else handleConsonant(jamo, bridge)
        return true
    }

    private fun handleConsonant(c: Char, bridge: EditorBridge) {
        when {
            // empty -> start a new syllable with a leading consonant
            cho == null && jung == null -> {
                cho = c
                bridge.setComposing(compose())
            }
            // leading consonant only, no vowel yet -> the new consonant starts a fresh syllable
            cho != null && jung == null -> rollover(bridge) { cho = c }
            // L + V, no 받침 yet -> attach as 받침 if it can be one, else roll over
            jung != null && jong == null -> {
                if (Hangul.jongIndex(c) > 0) {
                    jong = c
                    bridge.setComposing(compose())
                } else {
                    rollover(bridge) { cho = c }
                }
            }
            // already has a 받침 -> try to form a double 받침, else roll over
            else -> {
                val combined = Hangul.combineJong(jong!!, c)
                if (combined != null) {
                    jong = combined
                    bridge.setComposing(compose())
                } else {
                    rollover(bridge) { cho = c }
                }
            }
        }
    }

    private fun handleVowel(v: Char, bridge: EditorBridge) {
        when {
            // empty -> lone vowel
            cho == null && jung == null -> {
                jung = v
                bridge.setComposing(compose())
            }
            // leading consonant, no vowel -> form LV
            cho != null && jung == null -> {
                jung = v
                bridge.setComposing(compose())
            }
            // L(+optional C) + V, no 받침 -> try a compound vowel, else new lone-vowel syllable
            jung != null && jong == null -> {
                val combined = Hangul.combineVowel(jung!!, v)
                if (combined != null) {
                    jung = combined
                    bridge.setComposing(compose())
                } else {
                    rollover(bridge) { jung = v }
                }
            }
            // L + V + 받침, then a vowel -> 도깨비불: the 받침 moves onto a new syllable
            else -> {
                val tail = jong!!
                val split = Hangul.splitJong(tail)
                if (split != null) {
                    // double 받침: keep the first half here, move the second onto the new syllable
                    jong = split.first
                    rollover(bridge) { cho = split.second; jung = v }
                } else {
                    // single 받침: it becomes the leading consonant of the new syllable
                    jong = null
                    rollover(bridge) { cho = tail; jung = v }
                }
            }
        }
    }

    override fun onBackspace(longPress: Boolean, bridge: EditorBridge): Boolean {
        if (longPress) {
            // Word-unit delete on committed text; commit anything in progress first.
            finishComposition(bridge)
            bridge.deleteWordBefore()
            return true
        }
        if (!isComposing) return false  // let the service delete one committed char

        when {
            jong != null -> {
                // double 받침 -> revert to its first component; else drop 받침
                jong = Hangul.splitJong(jong!!)?.first
            }
            jung != null -> {
                // compound vowel -> revert to first component; else (null) drop the vowel
                jung = Hangul.splitVowel(jung!!)
            }
            cho != null -> cho = null
        }

        if (isComposing) {
            bridge.setComposing(compose())
        } else {
            // Composing region is now empty: clear it.
            bridge.setComposing("")
            bridge.finishComposing()
        }
        return true
    }

    override fun onSpace(bridge: EditorBridge): Boolean {
        finishComposition(bridge)
        return false  // let the service insert a literal space
    }

    override fun onEnter(bridge: EditorBridge): Boolean {
        finishComposition(bridge)
        return false  // let the service send Enter
    }

    override fun finishComposition(bridge: EditorBridge) {
        if (isComposing) {
            bridge.setComposing(compose())
            bridge.finishComposing()
        }
        clear()
    }

    override fun reset() = clear()
}
