package com.kibo.ime.ui.settings

import android.view.KeyEvent
import com.kibo.ime.prefs.KeyBinding

/** Modifier bits Kibo recognizes for the switch key (spec §2). */
private val RELEVANT_META = KeyEvent.META_SHIFT_ON or KeyEvent.META_ALT_ON or
    KeyEvent.META_CTRL_ON or KeyEvent.META_FUNCTION_ON or KeyEvent.META_SYM_ON or
    KeyEvent.META_META_ON

fun normalizeMeta(metaState: Int): Int = metaState and RELEVANT_META

/** True if the binding includes at least one modifier (spec §2: 단일 글자키 금지). */
fun KeyBinding.hasModifier(): Boolean = metaState and RELEVANT_META != 0

/** Human keycaps list, e.g. ["fn", "Space"]. */
fun KeyBinding.keycaps(): List<String> {
    val mods = buildList {
        if (metaState and KeyEvent.META_FUNCTION_ON != 0) add("fn")
        if (metaState and KeyEvent.META_CTRL_ON != 0) add("Ctrl")
        if (metaState and KeyEvent.META_ALT_ON != 0) add("Alt")
        if (metaState and KeyEvent.META_SHIFT_ON != 0) add("Shift")
        if (metaState and KeyEvent.META_SYM_ON != 0) add("Sym")
        if (metaState and KeyEvent.META_META_ON != 0) add("Meta")
    }
    return mods + keyLabel(keyCode)
}

fun KeyBinding.describe(): String = keycaps().joinToString(" + ")

private fun keyLabel(keyCode: Int): String = when (keyCode) {
    KeyEvent.KEYCODE_SPACE -> "Space"
    KeyEvent.KEYCODE_ENTER -> "Enter"
    KeyEvent.KEYCODE_TAB -> "Tab"
    KeyEvent.KEYCODE_DEL -> "Bksp"
    else -> {
        val label = KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
        if (label.length == 1) label else label.lowercase().replaceFirstChar { it.uppercase() }
    }
}
