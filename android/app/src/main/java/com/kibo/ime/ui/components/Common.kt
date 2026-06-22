package com.kibo.ime.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/**
 * Keycap chip (design system `.kb-keycap`): surface fill, 1px border,
 * a 1px bottom "edge" shadow — used to render physical-key hints (fn + Space, etc.).
 */
@Composable
fun Keycap(label: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    val c = KiboTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp)
            .clip(Radius.md)
            .background(if (accent) c.accentSubtle else c.surface)
            .border(1.dp, if (accent) c.accent else c.borderStrong, Radius.md)
            .padding(horizontal = Space.s3, vertical = Space.s1),
    ) {
        Text(
            text = label,
            color = if (accent) c.accentText else c.text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
    }
}

/** Plus-joined keycaps, e.g. fn + Space. */
@Composable
fun KeyCombo(keys: List<String>, accent: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
        keys.forEachIndexed { i, k ->
            if (i > 0) Text("+", color = KiboTheme.colors.textSubtle, fontSize = 13.sp)
            Keycap(k, accent = accent)
        }
    }
}

/** Overline section label (design `.kb-section-label`). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = KiboTheme.colors.textSubtle,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 1.68.sp,
        modifier = modifier.padding(horizontal = Space.s5, vertical = Space.s3),
    )
}

/** Segmented control (design `.kb-segmented`) — e.g. 복사 히스토리 / 텍스트 프리셋. */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = KiboTheme.colors
    Row(
        modifier = modifier
            .clip(Radius.lg)
            .background(c.surfaceSunken)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selected
            val bg by animateColorAsState(if (active) c.surface else Color.Transparent, label = "seg")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(bg)
                    .clickable { onSelect(i) },
            ) {
                Text(
                    text = label,
                    color = if (active) c.textStrong else c.textMuted,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** A round toolbar/icon button slot. */
@Composable
fun IconSlot(
    active: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val c = KiboTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(Radius.md)
            .background(if (active) c.accent else Color.Transparent)
            .clickable(onClick = onClick),
        content = { content() },
    )
}
