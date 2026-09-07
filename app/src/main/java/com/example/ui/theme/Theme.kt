package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ColorIncome,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = ColorSavings,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = ColorFreeFunds,
    onTertiary = Color.Black,
    error = ColorExpense,
    onError = Color.White,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = AppBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Neon dark mode as requested in specifications
    dynamicColor: Boolean = false, // Keep consistent custom neon palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
