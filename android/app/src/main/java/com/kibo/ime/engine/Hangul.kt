package com.kibo.ime.engine

/**
 * Hangul syllable arithmetic + 두벌식(2-set) jamo logic (spec §3).
 *
 * Works in *compatibility jamo* (U+3131..U+3163) as the lingua franca, then
 * composes finished syllables via the Unicode algorithm
 * (0xAC00 + (cho*21 + jung)*28 + jong).
 */
object Hangul {

    const val SBASE = 0xAC00

    /** Leading consonants (초성), 19. */
    val CHO = listOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
    )

    /** Medial vowels (중성), 21. */
    val JUNG = listOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ',
    )

    /** Trailing consonants (종성), index 0 = none, 28 total. */
    val JONG = listOf(
        ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
    )

    /** Vowel pairs that combine: (first, second) -> compound. */
    private val VOWEL_COMBINE = mapOf(
        ('ㅗ' to 'ㅏ') to 'ㅘ', ('ㅗ' to 'ㅐ') to 'ㅙ', ('ㅗ' to 'ㅣ') to 'ㅚ',
        ('ㅜ' to 'ㅓ') to 'ㅝ', ('ㅜ' to 'ㅔ') to 'ㅞ', ('ㅜ' to 'ㅣ') to 'ㅟ',
        ('ㅡ' to 'ㅣ') to 'ㅢ',
    )

    /** Trailing-consonant pairs that combine into a double 받침. */
    private val JONG_COMBINE = mapOf(
        ('ㄱ' to 'ㅅ') to 'ㄳ', ('ㄴ' to 'ㅈ') to 'ㄵ', ('ㄴ' to 'ㅎ') to 'ㄶ',
        ('ㄹ' to 'ㄱ') to 'ㄺ', ('ㄹ' to 'ㅁ') to 'ㄻ', ('ㄹ' to 'ㅂ') to 'ㄼ',
        ('ㄹ' to 'ㅅ') to 'ㄽ', ('ㄹ' to 'ㅌ') to 'ㄾ', ('ㄹ' to 'ㅍ') to 'ㄿ',
        ('ㄹ' to 'ㅎ') to 'ㅀ', ('ㅂ' to 'ㅅ') to 'ㅄ',
    )

    /** Reverse of [VOWEL_COMBINE] for backspace decomposition: compound -> first. */
    private val VOWEL_SPLIT = VOWEL_COMBINE.entries.associate { (k, v) -> v to k.first }

    /** compound jong -> (remaining single, the consonant that splits off). */
    private val JONG_SPLIT = JONG_COMBINE.entries.associate { (k, v) -> v to (k.first to k.second) }

    fun choIndex(c: Char) = CHO.indexOf(c)
    fun jungIndex(c: Char) = JUNG.indexOf(c)
    fun jongIndex(c: Char) = JONG.indexOf(c).let { if (it <= 0) -1 else it }

    fun isVowel(c: Char) = JUNG.contains(c)
    fun isConsonant(c: Char) = CHO.contains(c) || JONG.contains(c)

    fun combineVowel(a: Char, b: Char): Char? = VOWEL_COMBINE[a to b]
    fun combineJong(a: Char, b: Char): Char? = JONG_COMBINE[a to b]
    fun splitVowel(c: Char): Char? = VOWEL_SPLIT[c]
    fun splitJong(c: Char): Pair<Char, Char>? = JONG_SPLIT[c]

    /** Compose a finished syllable from compat jamo. [jong] may be null/space. */
    fun compose(cho: Char, jung: Char, jong: Char?): Char {
        val l = CHO.indexOf(cho)
        val v = JUNG.indexOf(jung)
        val t = if (jong == null) 0 else JONG.indexOf(jong).coerceAtLeast(0)
        return (SBASE + (l * 21 + v) * 28 + t).toChar()
    }

    /**
     * 두벌식 표준 layout: Latin key (post-shift) -> compatibility jamo.
     * Shift only matters for the double consonants and ㅒ/ㅖ.
     */
    private val LAYOUT = buildMap {
        // unshifted
        putAll(
            mapOf(
                'q' to 'ㅂ', 'w' to 'ㅈ', 'e' to 'ㄷ', 'r' to 'ㄱ', 't' to 'ㅅ',
                'y' to 'ㅛ', 'u' to 'ㅕ', 'i' to 'ㅑ', 'o' to 'ㅐ', 'p' to 'ㅔ',
                'a' to 'ㅁ', 's' to 'ㄴ', 'd' to 'ㅇ', 'f' to 'ㄹ', 'g' to 'ㅎ',
                'h' to 'ㅗ', 'j' to 'ㅓ', 'k' to 'ㅏ', 'l' to 'ㅣ',
                'z' to 'ㅋ', 'x' to 'ㅌ', 'c' to 'ㅊ', 'v' to 'ㅍ',
                'b' to 'ㅠ', 'n' to 'ㅜ', 'm' to 'ㅡ',
            )
        )
        // shifted differences
        putAll(
            mapOf(
                'Q' to 'ㅃ', 'W' to 'ㅉ', 'E' to 'ㄸ', 'R' to 'ㄲ', 'T' to 'ㅆ',
                'O' to 'ㅒ', 'P' to 'ㅖ',
            )
        )
    }

    /** Map a physical Latin key (with case = shift) to a 두벌식 jamo, or null if not a letter key. */
    fun jamoFor(key: Char): Char? {
        LAYOUT[key]?.let { return it }
        LAYOUT[key.lowercaseChar()]?.let { return it }
        // Already a compatibility jamo (e.g. the on-screen keyboard sends ㅂ directly).
        if (isVowel(key) || CHO.contains(key) || JONG.contains(key)) return key
        return null
    }
}
