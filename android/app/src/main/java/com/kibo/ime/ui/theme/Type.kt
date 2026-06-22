package com.kibo.ime.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kibo.ime.R

/** Pyeojin Gothic (펴진고딕) — self-hosted, weights 300–900. */
val PyeojinGothic = FontFamily(
    Font(R.font.pyeojin_light, FontWeight.Light),
    Font(R.font.pyeojin_regular, FontWeight.Normal),
    Font(R.font.pyeojin_medium, FontWeight.Medium),
    Font(R.font.pyeojin_semibold, FontWeight.SemiBold),
    Font(R.font.pyeojin_bold, FontWeight.Bold),
    Font(R.font.pyeojin_extrabold, FontWeight.ExtraBold),
    Font(R.font.pyeojin_black, FontWeight.Black),
)

private val tightHeights = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * Type scale from the handoff (size / line-height / tracking).
 * Mapped onto Material3 slots so stock components pick up the family.
 */
val KiboTypography = Typography(
    // display-lg 44 / 1.08 / −0.024em
    displayLarge = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp, lineHeight = 47.5.sp, letterSpacing = (-0.024).em,
        lineHeightStyle = tightHeights,
    ),
    // h1 34 / 1.18 / −0.018em
    headlineLarge = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.018).em,
    ),
    // h2 28 / 1.22 / −0.014em
    headlineMedium = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.014).em,
    ),
    // h3 22 / 1.30 / −0.010em
    headlineSmall = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.6.sp, letterSpacing = (-0.010).em,
    ),
    // h4 18 / 1.40
    titleLarge = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    // body-lg 18 / 1.62
    bodyLarge = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 29.sp,
    ),
    // body 16 / 1.60
    bodyMedium = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 25.6.sp,
    ),
    // body-sm 14 / 1.55
    bodySmall = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 21.7.sp,
    ),
    // caption 13 / 1.45
    labelMedium = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.85.sp,
    ),
    // overline 12 / 1.30 / 0.14em uppercase
    labelSmall = TextStyle(
        fontFamily = PyeojinGothic, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 15.6.sp, letterSpacing = 0.14.em,
    ),
)
