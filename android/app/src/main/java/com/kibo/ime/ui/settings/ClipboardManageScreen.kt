package com.kibo.ime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kibo.ime.prefs.ClipboardSettings
import com.kibo.ime.ui.components.SegmentedTabs
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/** Full clipboard management — history + presets (spec §8). */
@Composable
fun ClipboardManageScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val c = KiboTheme.colors
    var tab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("클립보드", onBack)
        SegmentedTabs(
            options = listOf("복사 히스토리", "텍스트 프리셋"),
            selected = tab, onSelect = { tab = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s4, vertical = Space.s2),
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (tab == 0) HistoryTab(vm) else PresetTab(vm)
        }
    }
}

@Composable
private fun HistoryTab(vm: SettingsViewModel) {
    val settings by vm.settings.collectAsState()
    val history by vm.history.collectAsState()
    val c = KiboTheme.colors
    val s = settings.clipboard

    Column(Modifier.fillMaxSize()) {
        SectionTitle("설정")
        KiboCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s3)) {
                Text("히스토리 보관", color = c.textStrong, fontSize = 16.sp, modifier = Modifier.weight(1f))
                listOf(20, 50, 100).forEach { n ->
                    val active = s.maxCount == n
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = Space.s2)
                            .clip(Radius.pill)
                            .background(if (active) c.accent else c.surfaceSubtle)
                            .clickable { vm.setClipboardSettings(s.copy(maxCount = n)) }
                            .padding(horizontal = Space.s4, vertical = Space.s2),
                    ) { Text("최대 ${n}개", color = if (active) c.textOnAccent else c.textMuted, fontSize = 13.sp) }
                }
            }
            RowDivider()
            SwitchRow("중복 제거", checked = s.dedupe) { vm.setClipboardSettings(s.copy(dedupe = it)) }
            RowDivider()
            SwitchRow("비밀번호 칸 제외", subtitle = "비밀번호 입력칸은 히스토리에 저장하지 않음", checked = s.excludePasswords) {
                vm.setClipboardSettings(s.copy(excludePasswords = it))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s5, vertical = Space.s3)) {
            Text("항목 ${history.size}", color = c.textMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("전체 지우기", color = c.dangerText, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { vm.clearHistory() })
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = Space.s4)) {
            items(history.size) { i ->
                val item = history[i]
                ManageRow(
                    text = item.text,
                    pinned = item.pinned,
                    onPin = { vm.toggleHistoryPin(item.id) },
                    onEdit = null,
                    onDelete = { vm.deleteHistory(item.id) },
                )
            }
        }
    }
}

@Composable
private fun PresetTab(vm: SettingsViewModel) {
    val presets by vm.presets.collectAsState()
    var editing by remember { mutableStateOf<Pair<Long?, String>?>(null) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = Space.s4, vertical = Space.s2)) {
            items(presets.size) { i ->
                val item = presets[i]
                ManageRow(
                    text = item.text,
                    pinned = item.pinned,
                    onPin = { vm.togglePresetPin(item.id) },
                    onEdit = { editing = item.id to item.text },
                    onDelete = { vm.deletePreset(item.id) },
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(Space.s4)) {
            PrimaryButton("프리셋 추가", Modifier.fillMaxWidth()) { editing = null to "" }
        }
    }

    editing?.let { (id, text) ->
        PresetEditDialog(
            initial = text,
            onConfirm = { v ->
                if (id == null) vm.addPreset(v) else vm.updatePreset(id, v)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun ManageRow(text: String, pinned: Boolean, onPin: () -> Unit, onEdit: (() -> Unit)?, onDelete: () -> Unit) {
    val c = KiboTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(Radius.md)
            .background(c.surface)
            .padding(horizontal = Space.s4, vertical = Space.s3),
    ) {
        Icon(Icons.Outlined.DragIndicator, null, tint = c.textSubtle, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Space.s3))
        Text(text, color = c.text, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(
            Icons.Outlined.PushPin, "고정",
            tint = if (pinned) c.accentText else c.textSubtle,
            modifier = Modifier.size(20.dp).clickable(onClick = onPin),
        )
        if (onEdit != null) {
            Spacer(Modifier.width(Space.s3))
            Icon(Icons.Outlined.Edit, "편집", tint = c.textMuted, modifier = Modifier.size(20.dp).clickable(onClick = onEdit))
        }
        Spacer(Modifier.width(Space.s3))
        Icon(Icons.Outlined.DeleteOutline, "삭제", tint = c.textMuted, modifier = Modifier.size(22.dp).clickable(onClick = onDelete))
    }
}

@Composable
private fun PresetEditDialog(initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val c = KiboTheme.colors
    var value by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().clip(Radius.xl).background(c.surface).padding(Space.s5)) {
            Text(if (initial.isEmpty()) "프리셋 추가" else "프리셋 편집", color = c.textStrong, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(Space.s4))
            Box(
                Modifier.fillMaxWidth().clip(Radius.md).background(c.surfaceSubtle).padding(Space.s4),
            ) {
                BasicTextField(
                    value = value, onValueChange = { value = it },
                    textStyle = TextStyle(color = c.text, fontSize = 16.sp),
                    cursorBrush = SolidColor(c.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(Space.s4))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlineButton("취소", Modifier.weight(1f), onClick = onDismiss)
                PrimaryButton("저장", Modifier.weight(1f), enabled = value.isNotBlank()) { onConfirm(value.trim()) }
            }
        }
    }
}
