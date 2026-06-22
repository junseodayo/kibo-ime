package com.kibo.ime.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing — 4px grid (--space-1…14). */
object Space {
    val s1 = 2.dp
    val s2 = 4.dp
    val s3 = 8.dp
    val s4 = 12.dp
    val s5 = 16.dp
    val s6 = 20.dp
    val s7 = 24.dp
    val s8 = 32.dp
    val s9 = 40.dp
    val s10 = 48.dp
    val s11 = 64.dp
    val s12 = 80.dp
    val s13 = 96.dp
    val s14 = 128.dp
}

/** Radius — sharp. Default UI uses 4–6px; pills only for switches/sliders/dots. */
object Radius {
    val xs = RoundedCornerShape(2.dp)
    val sm = RoundedCornerShape(3.dp)
    val md = RoundedCornerShape(4.dp)
    val lg = RoundedCornerShape(6.dp)
    val xl = RoundedCornerShape(10.dp)
    val pill = RoundedCornerShape(999.dp)
}

/** Motion — quiet, no bounce. */
object Motion {
    const val DUR_FAST = 120
    const val DUR_MED = 180
    const val DUR_SLOW = 280
}
