package com.liteweight.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenDark = Color(0xFF1B5E20)

private val LightColors =
    lightColorScheme(
        primary = GreenPrimary,
        onPrimary = Color.White,
        secondary = GreenDark,
        onSecondary = Color.White,
        background = Color(0xFFF7F8F7),
        onBackground = Color(0xFF1A1C19),
        surface = Color.White,
        onSurface = Color(0xFF1A1C19),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF81C784),
        onPrimary = Color(0xFF003910),
        secondary = Color(0xFFA5D6A7),
        onSecondary = Color(0xFF003910),
    )

@Composable
fun LiteWeightTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
