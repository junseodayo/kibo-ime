package com.kibo.ime.ui.settings

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibo.ime.ui.components.Keycap
import com.kibo.ime.ui.theme.KiboPalette
import com.kibo.ime.ui.theme.KiboTheme
import com.kibo.ime.ui.theme.Radius
import com.kibo.ime.ui.theme.Space
import kotlin.math.roundToInt

/** Theme / accent color (spec §13). Quick chips + a full HSV picker. */
@Composable
fun ThemeScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val c = KiboTheme.colors
    val current = settings.accentColor?.let { Color(it) } ?: KiboPalette.Lime400
    var showPicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("테마 · 색상", onBack)
        Text(
            "툴바와 온스크린 키보드의 강조색을 바꿉니다. (추후 정식 항목 — §13)",
            color = c.textMuted, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = Space.s5, vertical = Space.s2),
        )

        // Live preview
        SectionTitle("미리보기")
        Box(Modifier.padding(horizontal = Space.s4)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(Radius.lg).background(c.surface).border(1.dp, c.border, Radius.lg).padding(Space.s5),
            ) {
                Keycap("가", accent = true)
                Spacer(Modifier.width(Space.s4))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(36.dp).clip(Radius.md).background(current).padding(horizontal = Space.s5),
                ) { Text("강조색", color = KiboPalette.Greige900, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(Radius.pill).background(current).border(1.dp, c.borderStrong, Radius.pill))
            }
        }

        // Quick chips
        SectionTitle("빠른 선택")
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.s5), horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
            QUICK_COLORS.forEach { color ->
                val selected = color.toArgb() == current.toArgb()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(Radius.pill)
                        .background(color)
                        .border(if (selected) 3.dp else 1.dp, if (selected) c.textStrong else c.borderStrong, Radius.pill)
                        .clickable { vm.setAccentColor(color.toArgb()) },
                )
            }
        }

        Spacer(Modifier.height(Space.s4))
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.s4), horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).height(44.dp).clip(Radius.lg).border(1.dp, c.borderStrong, Radius.lg)
                    .clickable { showPicker = !showPicker }.padding(horizontal = Space.s5),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Palette, null, tint = c.text, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.s2))
                    Text("직접 선택", color = c.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            OutlineButton("기본값", onClick = { vm.setAccentColor(null) })
        }

        if (showPicker) {
            HsvPicker(initial = current) { picked -> vm.setAccentColor(picked.toArgb()) }
        }
        Spacer(Modifier.height(Space.s8))
    }
}

private val QUICK_COLORS = listOf(
    KiboPalette.Lime400,
    Color(0xFF4F9D6B), // green
    Color(0xFF4A6CF0), // blue
    Color(0xFFE0A33A), // amber
    Color(0xFFD9442B), // red
    Color(0xFF9B5DE5), // violet
)

@Composable
private fun HsvPicker(initial: Color, onPick: (Color) -> Unit) {
    val c = KiboTheme.colors
    val hsv = remember {
        FloatArray(3).also { AndroidColor.colorToHSV(initial.toArgb(), it) }
    }
    var hue by remember { mutableStateOf(hsv[0]) }
    var sat by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }

    fun emit() {
        onPick(Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, value))))
    }

    val hueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))

    Column(Modifier.padding(Space.s4)) {
        // Saturation / Value box
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(Radius.lg)
                .pointerInput(hue) {
                    detectTapGestures { off ->
                        sat = (off.x / size.width).coerceIn(0f, 1f)
                        value = (1f - off.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                }
                .pointerInput(hue) {
                    detectDragGestures { change, _ ->
                        sat = (change.position.x / size.width).coerceIn(0f, 1f)
                        value = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                val cx = sat * size.width
                val cy = (1f - value) * size.height
                drawCircle(Color.White, radius = 9f, center = Offset(cx, cy))
                drawCircle(Color.Black, radius = 9f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            }
        }

        Spacer(Modifier.height(Space.s4))
        // Hue slider
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(Radius.pill)
                .pointerInput(Unit) {
                    detectTapGestures { off -> hue = (off.x / size.width).coerceIn(0f, 1f) * 360f; emit() }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ -> hue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f; emit() }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val colors = (0..360 step 60).map { Color(AndroidColor.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))) }
                drawRect(Brush.horizontalGradient(colors))
                val x = hue / 360f * size.width
                drawCircle(Color.White, radius = size.height / 2f - 2f, center = Offset(x, size.height / 2f), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
            }
        }

        Spacer(Modifier.height(Space.s4))
        // HEX field
        val argb = AndroidColor.HSVToColor(floatArrayOf(hue, sat, value))
        var hexText by remember(argb) { mutableStateOf(String.format("#%06X", 0xFFFFFF and argb)) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("HEX", color = c.textMuted, fontSize = 13.sp)
            Spacer(Modifier.width(Space.s3))
            Box(Modifier.width(120.dp).clip(Radius.md).background(c.surfaceSubtle).padding(horizontal = Space.s4, vertical = Space.s3)) {
                BasicTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input
                        runCatching {
                            val parsed = AndroidColor.parseColor(if (input.startsWith("#")) input else "#$input")
                            val out = FloatArray(3)
                            AndroidColor.colorToHSV(parsed, out)
                            hue = out[0]; sat = out[1]; value = out[2]
                            emit()
                        }
                    },
                    textStyle = TextStyle(color = c.text, fontSize = 15.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(c.accent),
                )
            }
            Spacer(Modifier.width(Space.s3))
            Box(Modifier.size(28.dp).clip(Radius.md).background(Color(argb)).border(1.dp, c.borderStrong, Radius.md))
        }
    }
}
