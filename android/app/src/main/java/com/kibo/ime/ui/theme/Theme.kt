package com.kibo.ime.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Access to Kibo's extended semantic colors anywhere under [KiboTheme]. */
val LocalKiboColors = staticCompositionLocalOf { lightKiboColors() }

object KiboTheme {
    val colors: KiboColors
        @Composable get() = LocalKiboColors.current
}

/**
 * Root theme. [accentOverride] supports the §13 color picker (recolor toolbar/keyboard);
 * pass null to use the default acid lime.
 */
@Composable
fun KiboTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentOverride: Color? = null,
    content: @Composable () -> Unit,
) {
    val accent = accentOverride ?: KiboPalette.Lime400
    val kibo = if (darkTheme) darkKiboColors(accent) else lightKiboColors(accent)

    // Bridge a subset into Material3 so stock components are on-brand.
    val material = if (darkTheme) {
        darkColorScheme(
            primary = kibo.accent,
            onPrimary = kibo.textOnAccent,
            background = kibo.bg,
            onBackground = kibo.text,
            surface = kibo.surface,
            onSurface = kibo.text,
            surfaceVariant = kibo.surfaceSubtle,
            onSurfaceVariant = kibo.textMuted,
            outline = kibo.border,
            error = kibo.danger,
        )
    } else {
        lightColorScheme(
            primary = kibo.accent,
            onPrimary = kibo.textOnAccent,
            background = kibo.bg,
            onBackground = kibo.text,
            surface = kibo.surface,
            onSurface = kibo.text,
            surfaceVariant = kibo.surfaceSubtle,
            onSurfaceVariant = kibo.textMuted,
            outline = kibo.border,
            error = kibo.danger,
        )
    }

    CompositionLocalProvider(LocalKiboColors provides kibo) {
        MaterialTheme(
            colorScheme = material,
            typography = KiboTypography,
            content = content,
        )
    }
}
