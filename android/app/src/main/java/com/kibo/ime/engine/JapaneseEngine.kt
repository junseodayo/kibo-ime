package com.kibo.ime.engine

/**
 * 日本語 engine (spec §5): ローマ字 → かな → (漢字変換) → 候補選択 → 確定.
 *
 * The kana assembly (Romaji) is real; the kanji conversion is delegated to
 * [JapaneseConverter] — currently [StubJapaneseConverter], to be replaced by Mozc.
 *
 * Flow:
 *  - letters build the ひらがな reading (shown as the composing region);
 *  - **Space** enters conversion and then cycles to the next candidate;
 *  - **Enter** confirms the active candidate.
 * No numeric candidate selection (spec §5).
 */
class JapaneseEngine(
    private val converter: JapaneseConverter = StubJapaneseConverter(),
) : InputEngine {

    override val language = Language.JAPANESE

    private val romaji = StringBuilder()   // pending latin not yet formed into kana
    private val reading = StringBuilder()   // confirmed ひらがな reading
    private var converting = false
    private var candidateList: List<String> = emptyList()
    private var activeIndex = 0

    override val isComposing: Boolean
        get() = romaji.isNotEmpty() || reading.isNotEmpty()

    /** Composing display: while reading = kana + pending romaji; while converting = active candidate. */
    private fun display(): String =
        if (converting) candidateList.getOrElse(activeIndex) { reading.toString() }
        else reading.toString() + romaji.toString()

    override fun candidates(): CandidateState? =
        if (candidateList.isNotEmpty()) CandidateState(reading.toString(), candidateList, activeIndex)
        else null

    /** Recompute live suggestions for the current reading (shown while typing; no highlight). */
    private fun refreshSuggestions() {
        candidateList = if (reading.isNotEmpty()) converter.convert(reading.toString()) else emptyList()
        if (!converting) activeIndex = -1
    }

    override fun onCharacter(ch: Char, bridge: EditorBridge): Boolean {
        if (!ch.isLetter() && ch != '-' && ch != '\'') return false
        if (converting) {
            // A keystroke during conversion confirms the current candidate, then starts fresh.
            confirm(bridge)
        }
        romaji.append(ch.lowercaseChar())
        val (kana, rest) = Romaji.convert(romaji.toString())
        reading.append(kana)
        romaji.setLength(0)
        romaji.append(rest)
        bridge.setComposing(display())
        refreshSuggestions()  // live candidates while typing (spec §5)
        return true
    }

    override fun onSpace(bridge: EditorBridge): Boolean {
        if (!isComposing) return false  // literal space
        if (!converting) {
            // flush any trailing romaji that can stand alone (e.g. lone "n" -> ん)
            flushTrailingN()
            candidateList = converter.convert(reading.toString())
            if (candidateList.isEmpty()) return true
            converting = true
            activeIndex = 0
        } else {
            activeIndex = (activeIndex + 1) % candidateList.size
        }
        bridge.setComposing(display())
        return true
    }

    override fun onEnter(bridge: EditorBridge): Boolean {
        if (!isComposing) return false
        confirm(bridge)
        return true
    }

    override fun selectCandidate(index: Int, bridge: EditorBridge) {
        if (index !in candidateList.indices) return
        converting = true  // so display()/confirm use the tapped candidate, even from live mode
        activeIndex = index
        confirm(bridge)
    }

    override fun onBackspace(longPress: Boolean, bridge: EditorBridge): Boolean {
        if (longPress) { finishComposition(bridge); bridge.deleteWordBefore(); return true }
        if (!isComposing) return false
        when {
            converting -> converting = false  // back out of conversion to the plain reading
            romaji.isNotEmpty() -> romaji.deleteCharAt(romaji.length - 1)
            reading.isNotEmpty() -> reading.deleteCharAt(reading.length - 1)
        }
        if (isComposing) {
            refreshSuggestions()
            bridge.setComposing(display())
        } else {
            candidateList = emptyList()
            bridge.setComposing("")
            bridge.finishComposing()
        }
        return true
    }

    private fun flushTrailingN() {
        if (romaji.isNotEmpty()) {
            // best-effort: a leftover "n" becomes ん, otherwise drop unconvertible tail
            if (romaji.toString() == "n") reading.append('ん')
            romaji.setLength(0)
        }
    }

    private fun confirm(bridge: EditorBridge) {
        val text = display()
        bridge.setComposing(text)
        bridge.finishComposing()
        clear()
    }

    override fun finishComposition(bridge: EditorBridge) {
        if (isComposing) {
            flushTrailingN()
            bridge.setComposing(display())
            bridge.finishComposing()
        }
        clear()
    }

    private fun clear() {
        romaji.setLength(0)
        reading.setLength(0)
        converting = false
        candidateList = emptyList()
        activeIndex = 0
    }

    override fun reset() = clear()
}
