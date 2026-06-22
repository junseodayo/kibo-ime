package com.kibo.ime.ui.settings

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.focusable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.prefs.KeyBinding
import com.kibo.ime.ui.components.KeyCombo
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space

/**
 * Press-to-assign the language switch key (spec §2).
 * - Requires ≥1 modifier (단일 글자키 금지).
 * - Captured here means the event reached the app; a system-claimed combo never
 *   arrives, which is the natural "이 조합은 시스템이 사용 중" signal.
 */
@Composable
fun AssignKeyScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val c = KiboTheme.colors
    var captured by remember { mutableStateOf<KeyBinding?>(null) }
    val focus = remember { FocusRequester() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("언어 전환키", onBack)

        Box(
            Modifier
                .fillMaxWidth()
                .padding(Space.s4)
                .focusRequester(focus)
                .focusable()
                .onPreviewKeyEvent { ev ->
                    if (ev.type == KeyEventType.KeyDown) {
                        val native = ev.nativeKeyEvent
                        if (!KeyEvent.isModifierKey(native.keyCode)) {
                            captured = KeyBinding(native.keyCode, normalizeMeta(native.metaState))
                        }
                        true
                    } else ev.type == KeyEventType.KeyUp
                },
        ) {
            val binding = captured
            val valid = binding != null && binding.hasModifier()
            val boxColor = when {
                binding == null -> c.borderStrong
                valid -> c.accent
                else -> c.danger
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(Radius.lg)
                    .background(if (valid) c.accentSubtle else c.surface)
                    .border(2.dp, SolidColor(boxColor), Radius.lg)
                    .padding(Space.s5),
            ) {
                if (binding == null) {
                    Text("여기를 누른 상태에서 원하는 키 조합을 누르세요", color = c.textMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(Space.s3))
                    KeyCombo(KeyBinding.DEFAULT.keycaps())
                } else {
                    KeyCombo(binding.keycaps(), accent = valid)
                }
            }
        }
        LaunchedEffect(Unit) { focus.requestFocus() }

        // Status banner
        val binding = captured
        when {
            binding == null -> Banner(Icons.Outlined.Info, c.info, "현재 지정: ${settings.switchKey.describe()}")
            binding.hasModifier() -> Banner(Icons.Outlined.CheckCircle, c.success, "사용 가능")
            else -> Banner(Icons.Outlined.Info, c.danger, "최소 1개 수정자를 포함해야 합니다 (단일 글자키 금지)")
        }

        Text(
            "· 최소 1개 수정자 포함 — 단일 글자키 금지\n" +
                "· 조합을 눌렀는데 아무것도 잡히지 않으면 시스템이 선점한 조합입니다. 다른 조합을 누르세요.",
            color = c.textMuted, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s3),
        )

        Spacer(Modifier.height(Space.s3))
        Row(Modifier.fillMaxWidth().padding(Space.s4), horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
            OutlineButton("취소", Modifier.weight(1f), onClick = onBack)
            PrimaryButton(
                "이 조합으로 지정",
                Modifier.weight(1f),
                enabled = captured?.hasModifier() == true,
            ) {
                captured?.let { vm.setSwitchKey(it); onBack() }
            }
        }
    }
}

@Composable
private fun Banner(icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s5, vertical = Space.s2),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.height(18.dp))
        Spacer(Modifier.width(Space.s3))
        Text(text, color = KiboTheme.colors.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
