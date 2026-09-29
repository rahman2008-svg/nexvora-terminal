package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    DARK,
    AMOLED,
    LIGHT
}

enum class AccentChoice(val label: String, val primary: Color, val secondary: Color) {
    CYAN("Electric Cyan", ElectricCyan, NeonPurple),
    PURPLE("Neon Violet", NeonPurple, ElectricCyan),
    EMERALD("Matrix Emerald", MatrixEmerald, ElectricCyan),
    AMBER("Cyber Amber", BrightAmber, NeonPurple),
    CRIMSON("Hacker Red", AlertRed, BrightAmber)
}

fun getDarkColorScheme(accent: AccentChoice): ColorScheme = darkColorScheme(
    primary = accent.primary,
    onPrimary = Color.Black,
    primaryContainer = accent.primary.copy(alpha = 0.2f),
    onPrimaryContainer = accent.primary,
    secondary = accent.secondary,
    onSecondary = Color.White,
    secondaryContainer = accent.secondary.copy(alpha = 0.2f),
    onSecondaryContainer = accent.secondary,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f)
)

fun getAmoledColorScheme(accent: AccentChoice): ColorScheme = darkColorScheme(
    primary = accent.primary,
    onPrimary = Color.Black,
    primaryContainer = accent.primary.copy(alpha = 0.25f),
    onPrimaryContainer = accent.primary,
    secondary = accent.secondary,
    onSecondary = Color.White,
    secondaryContainer = accent.secondary.copy(alpha = 0.25f),
    onSecondaryContainer = accent.secondary,
    background = AmoledBackground,
    onBackground = TextPrimaryDark,
    surface = AmoledSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = AmoledBorder,
    outlineVariant = AmoledBorder.copy(alpha = 0.5f)
)

fun getLightColorScheme(accent: AccentChoice): ColorScheme = lightColorScheme(
    primary = accent.primary,
    onPrimary = Color.White,
    primaryContainer = accent.primary.copy(alpha = 0.15f),
    onPrimaryContainer = Color.Black,
    secondary = accent.secondary,
    onSecondary = Color.White,
    secondaryContainer = accent.secondary.copy(alpha = 0.15f),
    onSecondaryContainer = Color.Black,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightBorder.copy(alpha = 0.6f)
)

@Composable
fun NexVoraTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accent: AccentChoice = AccentChoice.CYAN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.AMOLED -> getAmoledColorScheme(accent)
        ThemeMode.DARK -> getDarkColorScheme(accent)
        ThemeMode.LIGHT -> getLightColorScheme(accent)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
