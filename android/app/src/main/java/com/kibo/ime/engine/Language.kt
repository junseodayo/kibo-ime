package com.kibo.ime.engine

/** The three input languages (spec §2). Default order 한국어 → English → 日本語. */
enum class Language(val tag: String, val label: String, val indicator: String) {
    KOREAN("ko", "한국어", "한"),
    ENGLISH("en", "English", "EN"),
    JAPANESE("ja", "日本語", "日");

    companion object {
        fun fromTag(tag: String?): Language? = entries.firstOrNull { it.tag == tag }
        val DEFAULT_ORDER = listOf(KOREAN, ENGLISH, JAPANESE)
    }
}
