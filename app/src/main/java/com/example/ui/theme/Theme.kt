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

// Casual Haven (Random public/social messaging)
private val CasualDarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8), // Warm Indigo
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFF43F5E), // Lively Rose
    onSecondary = Color.White,
    background = Color(0xFF0F172A), // Slate 900
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B), // Slate 800
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private val CasualLightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5), // Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF4F46E5),
    secondary = Color(0xFFE11D48),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC), // Slate 50
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0)
)

// Enterprise Hub (Sleek professional, secure and safe-for-work business messaging)
private val EnterpriseDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00A884), // WhatsApp Teal Green
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF005C53),
    onPrimaryContainer = Color(0xFFE1F5FE),
    secondary = Color(0xFF0288D1), // Business Blue
    onSecondary = Color.White,
    background = Color(0xFF111B21), // WhatsApp dark background
    onBackground = Color(0xFFE9EDF0),
    surface = Color(0xFF202C33), // Dark Slate
    onSurface = Color(0xFFE9EDF0),
    surfaceVariant = Color(0xFF2A3942),
    onSurfaceVariant = Color(0xFF8696A0),
    outline = Color(0xFF374248)
)

private val EnterpriseLightColorScheme = lightColorScheme(
    primary = Color(0xFF008069), // Classic business teal green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9FDD3), // WhatsApp message bubble green
    onPrimaryContainer = Color(0xFF111B21),
    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,
    background = Color(0xFFECE5DD), // Classic WhatsApp wallpaper color
    onBackground = Color(0xFF111B21),
    surface = Color.White,
    onSurface = Color(0xFF111B21),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF667781),
    outline = Color(0xFFE9EDF0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand's distinct aesthetic consistent
    appMode: String = "GAMING",
    content: @Composable () -> Unit
) {
    val colorScheme = when (appMode) {
        "CASUAL" -> if (darkTheme) CasualDarkColorScheme else CasualLightColorScheme
        "ENTERPRISE" -> if (darkTheme) EnterpriseDarkColorScheme else EnterpriseLightColorScheme
        else -> if (darkTheme) WeGistDarkColorScheme else WeGistLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
