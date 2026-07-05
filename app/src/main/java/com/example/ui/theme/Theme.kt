package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WeGistDarkColorScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003D30),
    onPrimaryContainer = NeonMint,
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF291047),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = CyberPink,
    onTertiary = Color.White,
    background = CyberSlateDark,
    onBackground = TextWhiteDark,
    surface = CyberCardDark,
    onSurface = TextWhiteDark,
    surfaceVariant = CyberBorderDark,
    onSurfaceVariant = TextGrayDark,
    outline = CyberBorderDark
)

private val WeGistLightColorScheme = lightColorScheme(
    primary = Color(0xFF7B1FA2), // High-contrast violet/purple for light mode readability
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E5F5),
    onPrimaryContainer = Color(0xFF7B1FA2),
    secondary = Color(0xFF007A60), // High-contrast secondary teal/mint
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF007A60),
    tertiary = CyberPink,
    onTertiary = Color.White,
    background = CyberSlateLight,
    onBackground = TextWhiteLight,
    surface = CyberCardLight,
    onSurface = TextWhiteLight,
    surfaceVariant = CyberBorderLight,
    onSurfaceVariant = TextGrayLight,
    outline = CyberBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand's distinct aesthetic consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        WeGistDarkColorScheme
    } else {
        WeGistLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
