package com.kibo.ime.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.engine.Language
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Space

/** 언어 순서 — reorderable list with engine subtitles (spec §2). */
@Composable
fun LanguageOrderScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val order = settings.languageOrder
    val c = KiboTheme.colors

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("언어 순서", onBack)
        Text(
            "전환키를 누를 때마다 이 순서로 순환합니다.",
            color = c.textMuted, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s2),
        )
        KiboCard {
            order.forEachIndexed { index, lang ->
                if (index > 0) RowDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s4),
                ) {
                    Icon(Icons.Outlined.DragIndicator, null, tint = c.textSubtle, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Space.s4))
                    Text("${index + 1}", color = c.textSubtle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(Space.s4))
                    Column(Modifier.weight(1f)) {
                        Text(lang.label, color = c.textStrong, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(engineSubtitle(lang), color = c.textMuted, fontSize = 13.sp)
                    }
                    // Up/down reorder (drag handle shown for affordance; arrows do the move).
                    ArrowButton(Icons.Outlined.KeyboardArrowUp, enabled = index > 0) {
                        vm.setLanguageOrder(order.swapped(index, index - 1))
                    }
                    ArrowButton(Icons.Outlined.KeyboardArrowDown, enabled = index < order.lastIndex) {
                        vm.setLanguageOrder(order.swapped(index, index + 1))
                    }
                }
            }
        }
    }
}

@Composable
private fun ArrowButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val c = KiboTheme.colors
    Icon(
        icon, null,
        tint = if (enabled) c.text else c.surfaceSunken,
        modifier = Modifier
            .size(32.dp)
            .padding(4.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    )
}

private fun engineSubtitle(lang: Language) = when (lang) {
    Language.KOREAN -> "두벌식 표준"
    Language.ENGLISH -> "Passthrough · Caps"
    Language.JAPANESE -> "Mozc · ローマ字→漢字"
}

private fun List<Language>.swapped(a: Int, b: Int): List<Language> =
    toMutableList().apply { val t = this[a]; this[a] = this[b]; this[b] = t }
