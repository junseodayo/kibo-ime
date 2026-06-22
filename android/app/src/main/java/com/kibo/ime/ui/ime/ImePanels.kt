package com.kibo.ime.ui.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.prefs.ClipItem
import com.kibo.ime.prefs.PresetItem
import com.kibo.ime.ui.components.SegmentedTabs
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

private const val PANEL_HEIGHT = 248

@Composable
private fun PanelScaffold(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClose: () -> Unit,
    body: @Composable () -> Unit,
) {
    val c = KiboTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(PANEL_HEIGHT.dp)
            .background(c.surface),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s5, vertical = Space.s3),
        ) {
            Icon(icon, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.s3))
            Text(title, color = c.textStrong, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (subtitle != null) {
                Spacer(Modifier.width(Space.s2))
                Text(subtitle, color = c.textSubtle, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(32.dp).clip(Radius.md).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "닫기", tint = c.textMuted, modifier = Modifier.size(18.dp)) }
        }
        body()
    }
}

@Composable
private fun CategoryChips(categories: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = KiboTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Space.s5, vertical = Space.s2),
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        categories.forEachIndexed { i, cat ->
            val active = i == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(Radius.pill)
                    .background(if (active) c.accent else c.surfaceSubtle)
                    .clickable { onSelect(i) }
                    .padding(horizontal = Space.s4, vertical = Space.s2),
            ) {
                Text(cat, color = if (active) c.textOnAccent else c.textMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun GlyphGrid(glyphs: List<String>, fontSize: Int, onPick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s4),
    ) {
        items(glyphs) { g ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(Radius.md).clickable { onPick(g) },
            ) { Text(g, fontSize = fontSize.sp) }
        }
    }
}

/** Emoji panel (spec §9). */
@Composable
fun EmojiPanel(onInsert: (String) -> Unit, onClose: () -> Unit) {
    var cat by remember { mutableIntStateOf(0) }
    val data = EmojiData.categories
    PanelScaffold(Icons.Outlined.SentimentSatisfiedAlt, "이모지", onClose = onClose) {
        CategoryChips(data.map { it.first }, cat) { cat = it }
        GlyphGrid(data[cat].second, fontSize = 24, onPick = onInsert)
    }
}

/** Symbol panel — includes a 일본어 기호 category (spec §9). */
@Composable
fun SymbolPanel(onInsert: (String) -> Unit, onClose: () -> Unit) {
    var cat by remember { mutableIntStateOf(0) }
    val data = SymbolData.categories
    PanelScaffold(Icons.Outlined.Tag, "기호", onClose = onClose) {
        CategoryChips(data.map { it.first }, cat) { cat = it }
        GlyphGrid(data[cat].second, fontSize = 19, onPick = onInsert)
    }
}

/** Clipboard panel — in use; tap to paste (spec §8). */
@Composable
fun ClipboardPanel(
    history: List<ClipItem>,
    presets: List<PresetItem>,
    onPaste: (String) -> Unit,
    onClose: () -> Unit,
) {
    val c = KiboTheme.colors
    var tab by remember { mutableIntStateOf(0) }
    PanelScaffold(Icons.Outlined.ContentPaste, "클립보드", subtitle = "· 탭하여 붙여넣기", onClose = onClose) {
        SegmentedTabs(
            options = listOf("복사 히스토리", "텍스트 프리셋"),
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s5, vertical = Space.s2),
        )
        androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth()) {
            if (tab == 0) {
                items(history.size) { idx ->
                    val item = history[idx]
                    ClipRow(item.text, pinned = item.pinned, meta = relativeTime(item.timestamp)) { onPaste(item.text) }
                }
            } else {
                items(presets.size) { idx ->
                    val item = presets[idx]
                    ClipRow(item.text, pinned = item.pinned, meta = null) { onPaste(item.text) }
                }
            }
        }
    }
}

@Composable
private fun ClipRow(text: String, pinned: Boolean, meta: String?, onClick: () -> Unit) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Space.s5, vertical = Space.s3),
    ) {
        if (pinned) {
            Icon(Icons.Outlined.PushPin, "고정", tint = c.accentText, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(Space.s3))
        }
        Text(
            text = text,
            color = c.text,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (meta != null) {
            Spacer(Modifier.width(Space.s3))
            Text(meta, color = c.textSubtle, fontSize = 12.sp)
        }
    }
}

private fun relativeTime(ts: Long): String {
    if (ts == 0L) return ""
    val diff = System.currentTimeMillis() - ts
    val min = diff / 60000
    return when {
        min < 1 -> "방금"
        min < 60 -> "${min}분 전"
        min < 1440 -> "${min / 60}시간 전"
        else -> "${min / 1440}일 전"
    }
}
