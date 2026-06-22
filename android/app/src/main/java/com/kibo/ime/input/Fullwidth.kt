package com.kibo.ime.input

/** 全角・半角 handling for the symbol layer in 日本語 mode (spec §6). */
object Fullwidth {
    /** Map an ASCII printable to its 全角 (full-width) form; space → U+3000. */
    fun toFullwidth(c: Char): Char = when {
        c == ' ' -> '　'
        c.code in 0x21..0x7E -> (c.code + 0xFEE0).toChar()
        else -> c
    }
}
