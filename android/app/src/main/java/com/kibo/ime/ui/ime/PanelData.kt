package com.kibo.ime.ui.ime

/**
 * Emoji panel content (spec §9). Grouped by *meaning* (not by raw Unicode block, which
 * mixes people/objects/food together). Ranges are listed in priority order: each code
 * point goes to the FIRST category that lists it (`seen` de-dups overlaps), so categories
 * stay coherent. Paint.hasGlyph drops anything the device font can't render (no tofu).
 * Computed once, lazily.
 */
object EmojiData {
    private val GROUPS: List<Pair<String, List<IntRange>>> = listOf(
        "표정" to listOf(0x1F600..0x1F64F, 0x1F910..0x1F92F, 0x1F970..0x1F97A, 0x1FAE0..0x1FAEF, 0x1F493..0x1F49F),
        "사람" to listOf(0x1F440..0x1F450, 0x1F464..0x1F487, 0x1F574..0x1F596, 0x1F90C..0x1F90F, 0x1F930..0x1F93E, 0x1F9B0..0x1F9B9, 0x1F9CD..0x1F9DF, 0x1FAC0..0x1FAC5, 0x1FAF0..0x1FAF8),
        "동물·자연" to listOf(0x1F300..0x1F335, 0x1F337..0x1F33C, 0x1F340..0x1F343, 0x1F400..0x1F43F, 0x1F980..0x1F9AE, 0x1FAB0..0x1FABF),
        "음식" to listOf(0x1F32D..0x1F32F, 0x1F336..0x1F336, 0x1F33D..0x1F33F, 0x1F344..0x1F37F, 0x1F950..0x1F96F, 0x1F9C0..0x1F9CB, 0x1FAD0..0x1FADF),
        "활동" to listOf(0x1F380..0x1F3CF, 0x1F3F8..0x1F3FA, 0x1F93F..0x1F94F),
        "여행" to listOf(0x1F3D5..0x1F3F0, 0x1F680..0x1F6FC),
        "물건" to listOf(0x1F451..0x1F463, 0x1F488..0x1F4FF, 0x1F500..0x1F53D, 0x1F550..0x1F567, 0x1F5A4..0x1F5FF, 0x1F9E0..0x1F9FF, 0x1FA70..0x1FA8F, 0x1FA90..0x1FAA8),
        "기호" to listOf(0x2600..0x26FF, 0x2700..0x27BF),
    )

    val categories: List<Pair<String, List<String>>> by lazy {
        val paint = android.graphics.Paint()
        val seen = HashSet<Int>()
        GROUPS.map { (name, ranges) ->
            val glyphs = ranges.asSequence()
                .flatMap { it.asSequence() }
                .filter { seen.add(it) }  // first category to list a code point wins
                .mapNotNull { cp ->
                    val glyph = String(Character.toChars(cp))
                    if (paint.hasGlyph(glyph)) glyph else null
                }
                .toList()
            name to glyphs
        }
    }
}

/** Symbol panel content — includes ₩ € ¥, arrows, and a 일본어 기호 set (spec §9). */
object SymbolData {
    val categories: List<Pair<String, List<String>>> = listOf(
        "자주" to "! ? . , … · : ; ' \" ` ( ) [ ] { } < > - – — _ / \\ | & @ # % ^ * + = ~ § ¶ •"
            .split(" "),
        "통화" to "₩ \$ € ¥ £ ¢ ₿ ₫ ₽ ₹ ₺ ₴ ₦ ฿ ₱ № % ‰ ™ © ®"
            .split(" "),
        "화살표" to "← → ↑ ↓ ↔ ↕ ↖ ↗ ↘ ↙ ⇐ ⇒ ⇑ ⇓ ⇔ ▲ ▼ ◀ ▶ « » ‹ ›"
            .split(" "),
        "수학" to "+ − × ÷ = ≠ ≈ ± ∞ √ ∑ ∏ ∫ π ∆ ∂ ≤ ≥ ∈ ∉ ⊂ ⊃ ∪ ∩ ° ′ ″"
            .split(" "),
        "일본어 기호" to "、 。 「 」 『 』 【 】 〔 〕 〈 〉 《 》 ・ ー 〜 ※ 々 〆 ○ ● ◎ △ ▽ □ ◆"
            .split(" "),
    )
}
