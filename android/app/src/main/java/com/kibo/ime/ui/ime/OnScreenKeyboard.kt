package com.kibo.ime.ui.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.engine.Language
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/**
 * On-screen keyboard — emergency fallback for hardware-key failure (spec §10).
 * Keys: flat surface fill, 1px border, a 1px bottom edge; function keys sit on
 * surface-subtle.
 */
@Composable
fun OnScreenKeyboard(state: ImeUiState, actions: ImeActions) {
    val c = KiboTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surfaceSunken)) {
        if (state.language == Language.KOREAN) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(c.warningSubtle).padding(horizontal = Space.s4, vertical = Space.s2),
            ) {
                Icon(Icons.Outlined.Warning, null, tint = c.warningText, modifier = Modifier.height(15.dp))
                Text(
                    "  물리 키보드 고장 시 비상용 폴백입니다",
                    color = c.warningText, fontSize = 12.sp,
                )
            }
        }

        val rows = layoutFor(state.language, state.caps)
        Column(Modifier.padding(Space.s2), verticalArrangement = Arrangement.spacedBy(Space.s2)) {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
                    // `for` keeps the RowScope receiver (forEach's lambda would not).
                    for (key in row) KeyButton(key, state, actions)
                }
            }
        }
    }
}

private sealed interface Key {
    val weight: Float
    data class Glyph(val label: String, val emit: String, override val weight: Float = 1f) : Key
    data class Func(val type: SoftKey, override val weight: Float = 1.5f) : Key
}

@Composable
private fun RowScope.KeyButton(key: Key, state: ImeUiState, actions: ImeActions) {
    val c = KiboTheme.colors
    val isFunc = key is Key.Func
    val activeShift = key is Key.Func && key.type == SoftKey.Shift && state.caps
    val activeSym = key is Key.Func && key.type == SoftKey.SymToggle && state.symLayer
    val active = activeShift || activeSym
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(key.weight)
            .height(44.dp)
            .clip(Radius.md)
            .background(
                when {
                    active -> c.accent
                    isFunc -> c.surfaceSubtle
                    else -> c.surface
                }
            )
            .border(1.dp, c.border, Radius.md)
            // 1px bottom edge (design `0 1px 0 --border-strong`)
            .padding(bottom = 1.dp)
            .clickable {
                when (key) {
                    is Key.Glyph -> actions.onSoftKey(SoftKey.Char(key.emit))
                    is Key.Func -> actions.onSoftKey(key.type)
                }
            },
    ) {
        when (key) {
            is Key.Glyph -> Text(key.label, color = c.text, fontSize = 18.sp)
            is Key.Func -> FuncIcon(key.type, if (active) c.textOnAccent else c.textMuted)
        }
    }
}

@Composable
private fun FuncIcon(type: SoftKey, tint: Color) {
    when (type) {
        SoftKey.Backspace -> Icon(Icons.Outlined.Backspace, "지우기", tint = tint, modifier = Modifier.height(20.dp))
        SoftKey.Enter -> Icon(Icons.AutoMirrored.Outlined.KeyboardReturn, "엔터", tint = tint, modifier = Modifier.height(20.dp))
        SoftKey.Shift -> Icon(Icons.Outlined.KeyboardArrowUp, "시프트", tint = tint, modifier = Modifier.height(20.dp))
        SoftKey.SymToggle -> Text("!#1", color = tint, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        SoftKey.Space -> Text("space", color = tint, fontSize = 13.sp)
        is SoftKey.Char -> {}
    }
}

/** Build the key rows for a language, honoring the soft-shift state. */
private fun layoutFor(language: Language, caps: Boolean): List<List<Key>> {
    return when (language) {
        Language.KOREAN -> koreanLayout(caps)
        else -> qwertyLayout(caps)
    }
}

private fun qwertyLayout(caps: Boolean): List<List<Key>> {
    fun row(s: String) = s.map { ch ->
        val label = if (caps) ch.uppercaseChar() else ch
        Key.Glyph(label.toString(), label.toString())
    }
    return listOf(
        row("qwertyuiop"),
        row("asdfghjkl"),
        listOf<Key>(Key.Func(SoftKey.Shift)) + row("zxcvbnm") + Key.Func(SoftKey.Backspace),
        listOf<Key>(
            Key.Func(SoftKey.SymToggle, 1.5f),
            Key.Func(SoftKey.Space, 5f),
            Key.Func(SoftKey.Enter, 1.5f),
        ),
    )
}

private fun koreanLayout(caps: Boolean): List<List<Key>> {
    // 두벌식 jamo. Shift swaps the relevant keys to their double/【ㅒㅖ】 forms.
    val r1 = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ")
    val r1s = listOf("ㅃ", "ㅉ", "ㄸ", "ㄲ", "ㅆ", "ㅛ", "ㅕ", "ㅑ", "ㅒ", "ㅖ")
    val r2 = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ")
    val r3 = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
    fun glyphs(list: List<String>): List<Key> = list.map { Key.Glyph(it, it) }
    return listOf(
        glyphs(if (caps) r1s else r1),
        glyphs(r2),
        listOf<Key>(Key.Func(SoftKey.Shift)) + glyphs(r3) + Key.Func(SoftKey.Backspace),
        listOf<Key>(
            Key.Func(SoftKey.SymToggle, 1.5f),
            Key.Func(SoftKey.Space, 5f),
            Key.Func(SoftKey.Enter, 1.5f),
        ),
    )
}
