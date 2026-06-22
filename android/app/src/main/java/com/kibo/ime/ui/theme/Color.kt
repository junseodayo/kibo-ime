package com.kibo.ime.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Kibo design tokens — colors. Mirrors design_files/tokens/colors.css.
 * Warm greige ramp + a single acid-lime accent. Light + dark.
 */
object KiboPalette {
    // Greige (warm neutral ramp)
    val Greige50 = Color(0xFFFAF9F6)
    val Greige100 = Color(0xFFF2F0EA)
    val Greige150 = Color(0xFFECE9E1)
    val Greige200 = Color(0xFFE7E4DD)
    val Greige300 = Color(0xFFD8D4C9)
    val Greige400 = Color(0xFFBBB6A9)
    val Greige500 = Color(0xFF9A958A)
    val Greige600 = Color(0xFF7A766C)
    val Greige700 = Color(0xFF5B584F)
    val Greige800 = Color(0xFF3A382F)
    val Greige900 = Color(0xFF1C1A16)
    val Greige950 = Color(0xFF100F0C)

    // Acid Lime accent
    val Lime100 = Color(0xFFECF7C6)
    val Lime300 = Color(0xFFDAF27E)
    val Lime400 = Color(0xFFC8F03C) // core accent
    val Lime500 = Color(0xFFB4DC2A)
    val Lime600 = Color(0xFF97BA15)
    val Lime700 = Color(0xFF6E8A0C)

    // Semantic hues
    val Red500 = Color(0xFFD9442B)
    val Red600 = Color(0xFFB7341E)
    val Amber500 = Color(0xFFE0A33A)
    val Green500 = Color(0xFF4F9D6B)
    val Blue500 = Color(0xFF4A6CF0)
}

/**
 * Resolved semantic aliases (one set per theme). Extends Material's color set
 * with Kibo-specific roles the design system uses (surfaceSunken, textMuted, etc.).
 */
data class KiboColors(
    val bg: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceSunken: Color,
    val textStrong: Color,
    val text: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val textOnAccent: Color,
    val border: Color,
    val borderStrong: Color,
    val accent: Color,
    val accentHover: Color,
    val accentPressed: Color,
    val accentSubtle: Color,
    val accentText: Color,
    val danger: Color,
    val dangerText: Color,
    val dangerSubtle: Color,
    val warning: Color,
    val warningText: Color,
    val warningSubtle: Color,
    val success: Color,
    val successText: Color,
    val successSubtle: Color,
    val info: Color,
    val isDark: Boolean,
)

fun lightKiboColors(accent: Color = KiboPalette.Lime400) = KiboColors(
    bg = KiboPalette.Greige50,
    surface = Color(0xFFFFFFFF),
    surfaceSubtle = KiboPalette.Greige100,
    surfaceSunken = KiboPalette.Greige150,
    textStrong = KiboPalette.Greige900,
    text = KiboPalette.Greige800,
    textMuted = KiboPalette.Greige600,
    textSubtle = KiboPalette.Greige500,
    textOnAccent = KiboPalette.Greige900,
    border = KiboPalette.Greige200,
    borderStrong = KiboPalette.Greige300,
    accent = accent,
    accentHover = KiboPalette.Lime500,
    accentPressed = KiboPalette.Lime600,
    accentSubtle = KiboPalette.Lime100,
    accentText = KiboPalette.Lime700,
    danger = KiboPalette.Red500,
    dangerText = KiboPalette.Red600,
    dangerSubtle = Color(0xFFFBEAE6),
    warning = KiboPalette.Amber500,
    warningText = Color(0xFFC2851C),
    warningSubtle = Color(0xFFFBF2DF),
    success = KiboPalette.Green500,
    successText = Color(0xFF3C8054),
    successSubtle = Color(0xFFE6F2EB),
    info = KiboPalette.Blue500,
    isDark = false,
)

fun darkKiboColors(accent: Color = KiboPalette.Lime400) = KiboColors(
    bg = KiboPalette.Greige950,
    surface = Color(0xFF1A1813),
    surfaceSubtle = Color(0xFF211F18),
    surfaceSunken = Color(0xFF141310),
    textStrong = KiboPalette.Greige50,
    text = KiboPalette.Greige150,
    textMuted = KiboPalette.Greige400,
    textSubtle = KiboPalette.Greige500,
    textOnAccent = KiboPalette.Greige950,
    border = Color(0xFF2C2A22),
    borderStrong = KiboPalette.Greige800,
    accent = accent,
    accentHover = KiboPalette.Lime300,
    accentPressed = KiboPalette.Lime500,
    accentSubtle = Color(0xFF2A3310),
    accentText = KiboPalette.Lime400,
    danger = Color(0xFFE5604A),
    dangerText = Color(0xFFEE8674),
    dangerSubtle = Color(0xFF2E1A16),
    warning = Color(0xFFE6B25A),
    warningText = Color(0xFFECC180),
    warningSubtle = Color(0xFF2C2412),
    success = Color(0xFF62B083),
    successText = Color(0xFF84C39E),
    successSubtle = Color(0xFF16241C),
    info = Color(0xFF7C96F5),
    isDark = true,
)
