package com.kibo.ime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(c.bg).padding(horizontal = Space.s4, vertical = Space.s4),
    ) {
        if (onBack != null) {
            Box(
                Modifier.size(36.dp).clip(Radius.md).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로", tint = c.text) }
            Spacer(Modifier.width(Space.s2))
        }
        Text(title, color = c.textStrong, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = KiboTheme.colors.textSubtle,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = Space.s5, top = Space.s5, bottom = Space.s2),
    )
}

@Composable
fun KiboCard(content: @Composable () -> Unit) {
    val c = KiboTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.s4, vertical = Space.s2)
            .clip(Radius.lg)
            .background(c.surface)
            .border(1.dp, c.border, Radius.lg),
    ) { content() }
}

@Composable
fun NavRow(title: String, subtitle: String? = null, trailing: String? = null, onClick: () -> Unit) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = Space.s5, vertical = Space.s4),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = c.textStrong, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) Text(subtitle, color = c.textMuted, fontSize = 13.sp)
        }
        if (trailing != null) {
            Text(trailing, color = c.textMuted, fontSize = 14.sp)
            Spacer(Modifier.width(Space.s2))
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = c.textSubtle)
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String? = null, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s5, vertical = Space.s3),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = c.textStrong, fontSize = 16.sp)
            if (subtitle != null) Text(subtitle, color = c.textMuted, fontSize = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = c.textOnAccent,
                checkedTrackColor = c.accent,
                uncheckedTrackColor = c.surfaceSunken,
                uncheckedBorderColor = c.borderStrong,
            ),
        )
    }
}

@Composable
fun RowDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(KiboTheme.colors.border))
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val c = KiboTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(Radius.lg)
            .background(if (enabled) c.accent else c.surfaceSunken)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Space.s6),
    ) {
        Text(text, color = if (enabled) c.textOnAccent else c.textSubtle, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun OutlineButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = KiboTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(44.dp)
            .clip(Radius.lg)
            .border(1.dp, c.borderStrong, Radius.lg)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.s5),
    ) {
        Text(text, color = c.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
