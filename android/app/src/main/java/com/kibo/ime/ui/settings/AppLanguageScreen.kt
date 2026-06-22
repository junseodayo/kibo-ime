package com.kibo.ime.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kibo.ime.engine.Language
import com.kibo.ime.prefs.AppLangMapping
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/** 앱별 기본 언어 (spec §11): master toggle + package→language mappings. */
@Composable
fun AppLanguageScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val c = KiboTheme.colors
    var showPicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("앱별 기본 언어", onBack)
        KiboCard {
            SwitchRow(
                "앱별 언어 사용",
                subtitle = "끄면 항상 직전 사용 언어를 유지합니다",
                checked = settings.appLangMasterOn,
                onChange = vm::setAppLangMasterOn,
            )
        }

        SectionTitle("앱 매핑")
        Text(
            "매핑이 없는 앱은 직전 사용 언어로 폴백합니다.",
            color = c.textMuted, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s2),
        )
        KiboCard {
            if (settings.appLangMappings.isEmpty()) {
                Text(
                    "아직 매핑이 없습니다.",
                    color = c.textSubtle, fontSize = 14.sp,
                    modifier = Modifier.padding(Space.s5),
                )
            }
            settings.appLangMappings.forEachIndexed { i, m ->
                if (i > 0) RowDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s4),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(m.appLabel, color = c.textStrong, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(m.packageName, color = c.textSubtle, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(m.language.label, color = c.accentText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.width(Space.s3))
                    Icon(
                        Icons.Outlined.DeleteOutline, "삭제", tint = c.textMuted,
                        modifier = Modifier.size(22.dp).clickable { vm.removeAppMapping(m.packageName) },
                    )
                }
            }
        }

        Spacer(Modifier.height(Space.s4))
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.s4)) {
            PrimaryButton("앱 매핑 추가", Modifier.fillMaxWidth()) { showPicker = true }
        }
    }

    if (showPicker) {
        AppMappingDialog(
            existing = settings.appLangMappings.map { it.packageName }.toSet(),
            onAdd = { vm.addAppMapping(it); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

private data class InstalledApp(val label: String, val pkg: String)

@Composable
private fun AppMappingDialog(existing: Set<String>, onAdd: (AppLangMapping) -> Unit, onDismiss: () -> Unit) {
    val c = KiboTheme.colors
    val context = LocalContext.current
    val apps = remember {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { InstalledApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .filter { it.pkg !in existing }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }
    var selected by remember { mutableStateOf<InstalledApp?>(null) }
    var lang by remember { mutableStateOf(Language.KOREAN) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .clip(Radius.xl)
                .background(c.surface)
                .padding(Space.s5),
        ) {
            Text("앱 선택", color = c.textStrong, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(Space.s3))
            LazyColumn(Modifier.weight(1f)) {
                items(apps) { app ->
                    val isSel = selected?.pkg == app.pkg
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Radius.md)
                            .background(if (isSel) c.accentSubtle else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { selected = app }
                            .padding(horizontal = Space.s4, vertical = Space.s3),
                    ) {
                        Text(app.label, color = c.textStrong, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (isSel) Text("선택됨", color = c.accentText, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(Space.s3))
            Text("언어", color = c.textMuted, fontSize = 13.sp)
            Spacer(Modifier.height(Space.s2))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
                Language.entries.forEach { l ->
                    val active = l == lang
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(Radius.pill)
                            .background(if (active) c.accent else c.surfaceSubtle)
                            .clickable { lang = l }
                            .padding(horizontal = Space.s4, vertical = Space.s2),
                    ) { Text(l.label, color = if (active) c.textOnAccent else c.textMuted, fontSize = 13.sp) }
                }
            }
            Spacer(Modifier.height(Space.s4))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlineButton("취소", Modifier.weight(1f), onClick = onDismiss)
                PrimaryButton("추가", Modifier.weight(1f), enabled = selected != null) {
                    selected?.let { onAdd(AppLangMapping(it.pkg, it.label, lang)) }
                }
            }
        }
    }
}
