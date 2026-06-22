package com.kibo.ime.engine

/**
 * Lightweight ローマ字 → ひらがな converter for the *reading* stage (spec §5).
 *
 * This intentionally covers only kana assembly — it is NOT the kanji converter.
 * The romaji→kana mapping is stable and device-independent, so it lives here;
 * the heavy kanji conversion + dictionary is delegated to [JapaneseConverter]
 * (to be backed by Mozc — see JapaneseEngine).
 */
object Romaji {

    // Longest-match table (up to 3 chars). Hiragana output.
    private val TABLE: Map<String, String> = buildMap {
        val base = mapOf(
            "a" to "あ", "i" to "い", "u" to "う", "e" to "え", "o" to "お",
            "ka" to "か", "ki" to "き", "ku" to "く", "ke" to "け", "ko" to "こ",
            "ga" to "が", "gi" to "ぎ", "gu" to "ぐ", "ge" to "げ", "go" to "ご",
            "sa" to "さ", "shi" to "し", "si" to "し", "su" to "す", "se" to "せ", "so" to "そ",
            "za" to "ざ", "ji" to "じ", "zi" to "じ", "zu" to "ず", "ze" to "ぜ", "zo" to "ぞ",
            "ta" to "た", "chi" to "ち", "ti" to "ち", "tsu" to "つ", "tu" to "つ", "te" to "て", "to" to "と",
            "da" to "だ", "di" to "ぢ", "du" to "づ", "de" to "で", "do" to "ど",
            "na" to "な", "ni" to "に", "nu" to "ぬ", "ne" to "ね", "no" to "の",
            "ha" to "は", "hi" to "ひ", "fu" to "ふ", "hu" to "ふ", "he" to "へ", "ho" to "ほ",
            "ba" to "ば", "bi" to "び", "bu" to "ぶ", "be" to "べ", "bo" to "ぼ",
            "pa" to "ぱ", "pi" to "ぴ", "pu" to "ぷ", "pe" to "ぺ", "po" to "ぽ",
            "ma" to "ま", "mi" to "み", "mu" to "む", "me" to "め", "mo" to "も",
            "ya" to "や", "yu" to "ゆ", "yo" to "よ",
            "ra" to "ら", "ri" to "り", "ru" to "る", "re" to "れ", "ro" to "ろ",
            "wa" to "わ", "wo" to "を", "nn" to "ん", "n'" to "ん",
            "kya" to "きゃ", "kyu" to "きゅ", "kyo" to "きょ",
            "sha" to "しゃ", "shu" to "しゅ", "sho" to "しょ",
            "cha" to "ちゃ", "chu" to "ちゅ", "cho" to "ちょ",
            "nya" to "にゃ", "nyu" to "にゅ", "nyo" to "にょ",
            "hya" to "ひゃ", "hyu" to "ひゅ", "hyo" to "ひょ",
            "mya" to "みゃ", "myu" to "みゅ", "myo" to "みょ",
            "rya" to "りゃ", "ryu" to "りゅ", "ryo" to "りょ",
            "gya" to "ぎゃ", "gyu" to "ぎゅ", "gyo" to "ぎょ",
            "ja" to "じゃ", "ju" to "じゅ", "jo" to "じょ",
            "bya" to "びゃ", "byu" to "びゅ", "byo" to "びょ",
            "pya" to "ぴゃ", "pyu" to "ぴゅ", "pyo" to "ぴょ",
            "-" to "ー",
        )
        putAll(base)
    }

    private val maxLen = TABLE.keys.maxOf { it.length }

    /**
     * Consume as much of [buffer] (lowercase romaji) as forms complete kana.
     * Returns (kana produced, leftover romaji still pending). Handles っ (double
     * consonant → sokuon) and ん before consonants.
     */
    fun convert(buffer: String): Pair<String, String> {
        val out = StringBuilder()
        var i = 0
        val s = buffer.lowercase()
        while (i < s.length) {
            // っ: a doubled consonant (not n) before a vowel-bearing unit
            val c = s[i]
            if (c != 'n' && isConsonant(c) && i + 1 < s.length && s[i + 1] == c) {
                out.append('っ'); i++; continue
            }
            // ん: a single 'n' followed by a consonant other than y
            if (c == 'n' && i + 1 < s.length && isConsonant(s[i + 1]) && s[i + 1] != 'y' && s[i + 1] != 'n') {
                out.append('ん'); i++; continue
            }
            var matched: String? = null
            val maxTake = minOf(maxLen, s.length - i)
            for (len in maxTake downTo 1) {
                val sub = s.substring(i, i + len)
                if (TABLE.containsKey(sub)) { matched = sub; break }
            }
            if (matched != null) {
                out.append(TABLE[matched]); i += matched.length
            } else {
                // Can't complete a kana yet — keep the rest as pending romaji.
                return out.toString() to s.substring(i)
            }
        }
        return out.toString() to ""
    }

    private fun isConsonant(c: Char) = c in 'a'..'z' && c !in "aiueo"

    /** ひらがな → カタカナ (spec §5 カタカナ変換 / §6 全角). */
    fun toKatakana(hira: String): String = buildString {
        for (ch in hira) {
            if (ch in 'ぁ'..'ゖ') append(ch + 0x60) else append(ch)
        }
    }
}
