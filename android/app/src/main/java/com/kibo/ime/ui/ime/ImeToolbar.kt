package com.kibo.ime.ui.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.engine.CandidateState
import com.kibo.ime.engine.Language
import com.kibo.ime.ui.components.IconSlot
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/** Mode indicator — shows current language + CAPS/SYM, tap a segment to switch (spec §2). */
@Composable
fun ModeIndicator(language: Language, caps: Boolean, sym: Boolean, onSelect: (Language) -> Unit) {
    val c = KiboTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
        Row(
            modifier = Modifier.clip(Radius.md).background(c.surfaceSunken).padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Language.entries.forEach { lang ->
                val active = lang == language
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 24.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (active) c.accent else Color.Transparent)
                        .clickable { onSelect(lang) }
                        .padding(horizontal = Space.s2),
                ) {
                    Text(
                        text = lang.indicator,
                        color = if (active) onAccent() else c.textSubtle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        // Drop the legacy font padding so CJK glyphs sit vertically centered.
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    )
                }
            }
        }
        if (caps || sym) {
            val label = buildString {
                if (caps) append("CAPS")
                if (caps && sym) append(" · ")
                if (sym) append("SYM")
            }
            Text(label, color = c.textMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

/** Japanese conversion candidate strip — Space=다음 / Enter=확정 (spec §5). */
@Composable
fun CandidateStrip(state: CandidateState, onPick: (Int) -> Unit) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(c.surface),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
        ) {
            state.candidates.forEachIndexed { i, cand ->
                val active = i == state.activeIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(34.dp)
                        .clip(Radius.md)
                        .background(if (active) c.accent else Color.Transparent)
                        .clickable { onPick(i) }
                        .padding(horizontal = 14.dp),
                ) {
                    Text(
                        text = cand,
                        color = if (active) c.textOnAccent else c.text,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 17.sp,
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = Space.s3, end = 14.dp),
        ) {
            Hint("Space", "다음")
            Hint("Enter", "확정")
        }
    }
}

@Composable
private fun Hint(key: String, label: String) {
    val c = KiboTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(key, color = c.textMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Spacer(Modifier.width(3.dp))
        Text(label, color = c.textSubtle, fontSize = 11.sp)
    }
}

/** Functional toolbar row (spec §9). */
@Composable
fun KiboToolbar(state: ImeUiState, actions: ImeActions) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)   // our toolbar row is the surface (white); accent is the strip below
            // Two-row layout (toolbar sits above the system nav bar). These insets align our
            // indicator (left) over the chevron and our icons (right) over the globe below.
            .padding(start = SystemNavLeftInset, end = SystemNavRightInset, top = Space.s3, bottom = Space.s3),
    ) {
        ModeIndicator(state.language, state.caps, state.symLayer, actions::switchLanguage)
        Spacer(Modifier.weight(1f))
        IconSlot(active = state.oskVisible, size = 34.dp, onClick = actions::toggleOsk) {
            Icon(Icons.Outlined.Keyboard, "온스크린 키보드", tint = iconTint(state.oskVisible), modifier = Modifier.size(18.dp))
        }
        IconSlot(active = state.panel == ImePanel.SYMBOL, size = 34.dp, onClick = { actions.openPanel(ImePanel.SYMBOL) }) {
            Icon(Icons.Outlined.Tag, "기호", tint = iconTint(state.panel == ImePanel.SYMBOL), modifier = Modifier.size(18.dp))
        }
        IconSlot(active = state.panel == ImePanel.EMOJI, size = 34.dp, onClick = { actions.openPanel(ImePanel.EMOJI) }) {
            Icon(Icons.Outlined.SentimentSatisfiedAlt, "이모지", tint = iconTint(state.panel == ImePanel.EMOJI), modifier = Modifier.size(18.dp))
        }
        IconSlot(active = state.panel == ImePanel.CLIPBOARD, size = 34.dp, onClick = { actions.openPanel(ImePanel.CLIPBOARD) }) {
            Icon(Icons.Outlined.ContentPaste, "클립보드", tint = iconTint(state.panel == ImePanel.CLIPBOARD), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun iconTint(active: Boolean): Color =
    if (active) onAccent() else Color(0xFF666666)  // #666 on the white toolbar; contrast color on the accent highlight

/** Color that contrasts the accent fill (active states): #666 on a light accent, white on a dark one. */
@Composable
private fun onAccent(): Color =
    if (KiboTheme.colors.accent.luminance() > 0.5f) Color(0xFF666666) else Color.White

// Left/right alignment of our toolbar items over the system nav buttons one row below
// (chevron left, globe right). Tune per device.
private val SystemNavLeftInset = 16.dp
private val SystemNavRightInset = 16.dp
