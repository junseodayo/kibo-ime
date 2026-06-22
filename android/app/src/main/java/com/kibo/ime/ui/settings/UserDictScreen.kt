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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kibo.ime.engine.Language
import com.kibo.ime.ui.components.SegmentedTabs
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/** User dictionary, tabbed by language (spec §7). */
@Composable
fun UserDictScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val c = KiboTheme.colors
    var tab by remember { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    val lang = Language.entries[tab]
    val entries = settings.userDict.filter { it.language == lang }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("사용자 사전", onBack)
        SegmentedTabs(
            options = Language.entries.map { it.label },
            selected = tab, onSelect = { tab = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s4, vertical = Space.s2),
        )
        if (lang == Language.JAPANESE) {
            Text(
                "日本語 항목은 변환 보정에 쓰입니다. 출시 시 NEologd 계열 사전과 통합될 예정입니다 (§5).",
                color = c.textMuted, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s2),
            )
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = Space.s4, vertical = Space.s2)) {
            if (entries.isEmpty()) {
                item {
                    Text("등록된 단어가 없습니다.", color = c.textSubtle, fontSize = 14.sp, modifier = Modifier.padding(Space.s5))
                }
            }
            items(entries.size) { i ->
                val e = entries[i]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(Radius.md).background(c.surface)
                        .padding(horizontal = Space.s4, vertical = Space.s3),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(e.word, color = c.textStrong, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        if (e.reading.isNotBlank()) {
                            val label = if (lang == Language.JAPANESE) "よみ: ${e.reading}" else e.reading
                            Text(label, color = c.textMuted, fontSize = 13.sp)
                        }
                    }
                    Icon(Icons.Outlined.DeleteOutline, "삭제", tint = c.textMuted,
                        modifier = Modifier.size(22.dp).clickable { vm.deleteDictEntry(e.id) })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(Space.s4)) {
            PrimaryButton("단어 추가", Modifier.fillMaxWidth()) { adding = true }
        }
    }

    if (adding) {
        DictAddDialog(
            lang = lang,
            onDismiss = { adding = false },
            onConfirm = { word, reading -> vm.addDictEntry(lang, word, reading); adding = false },
        )
    }
}

@Composable
private fun DictAddDialog(lang: Language, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    val c = KiboTheme.colors
    var word by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }
    val readingLabel = when (lang) {
        Language.JAPANESE -> "よみ (히라가나)"
        Language.KOREAN -> "분류/메모 (예: 고유명사)"
        Language.ENGLISH -> "note (optional)"
    }
    val wordLabel = if (lang == Language.JAPANESE) "漢字 / 단어" else "단어"

    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().clip(Radius.xl).background(c.surface).padding(Space.s5)) {
            Text("${lang.label} 단어 추가", color = c.textStrong, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(Space.s4))
            Field(wordLabel, word) { word = it }
            Spacer(Modifier.height(Space.s3))
            Field(readingLabel, reading) { reading = it }
            Spacer(Modifier.height(Space.s4))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlineButton("취소", Modifier.weight(1f), onClick = onDismiss)
                PrimaryButton("추가", Modifier.weight(1f), enabled = word.isNotBlank()) { onConfirm(word.trim(), reading.trim()) }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    val c = KiboTheme.colors
    Column {
        Text(label, color = c.textMuted, fontSize = 12.sp)
        Spacer(Modifier.height(Space.s2))
        Box(Modifier.fillMaxWidth().clip(Radius.md).background(c.surfaceSubtle).padding(Space.s4)) {
            BasicTextField(
                value = value, onValueChange = onChange,
                textStyle = TextStyle(color = c.text, fontSize = 16.sp),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
