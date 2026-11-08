package com.gamecontrol.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GameControlDarkColors = darkColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorOnPrimary,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = ColorOnPrimaryContainer,
    secondary = ColorSecondary,
    onSecondary = ColorOnSecondary,
    secondaryContainer = ColorSecondaryContainer,
    onSecondaryContainer = ColorOnSecondaryContainer,
    tertiary = ColorTertiary,
    onTertiary = ColorOnTertiary,
    tertiaryContainer = ColorTertiaryContainer,
    onTertiaryContainer = ColorOnTertiaryContainer,
    error = ColorError,
    onError = ColorOnError,
    background = ColorSurfaceBase,
    onBackground = ColorOnSurface,
    surface = ColorSurfaceBase,
    onSurface = ColorOnSurface,
    surfaceVariant = ColorSurfaceHighest,
    onSurfaceVariant = ColorOnSurfaceVariant,
    outline = ColorOutline,
    outlineVariant = ColorOutlineVariant,
)

@Composable
fun GameControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GameControlDarkColors,
        typography = GameControlTypography,
        content = content,
    )
}
