package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Hardcoded base constants for dark mode palette
val CyberSlateDark = Color(0xFF0A0A0C)      // Deep Background
val CyberCardDark = Color(0xFF141418)       // Card / Elevation layers
val CyberBorderDark = Color(0xFF24242C)     // Clean borders
val TextWhiteDark = Color(0xFFFFFFFF)        // Sharp text
val TextGrayDark = Color(0xFF9E9EAF)         // Secondary muted descriptions

// Hardcoded base constants for light mode palette
val CyberSlateLight = Color(0xFFF4F5F8)     // Deep Background
val CyberCardLight = Color(0xFFFFFFFF)      // Card / Elevation layers
val CyberBorderLight = Color(0xFFE5E7EB)    // Clean borders
val TextWhiteLight = Color(0xFF0A0A0C)      // Sharp text
val TextGrayLight = Color(0xFF5A5A65)       // Secondary muted descriptions

// Accent colors
val NeonMint = Color(0xFF00FFCC)        // Primary Accent (Teal)
val NeonPurple = Color(0xFFB15EFF)      // Secondary Accent (Violet)
val CyberPink = Color(0xFFFF2A85)       // Action highlight / Live badges
val AccentOrange = Color(0xFFFF9E00)     // Warm alert / Gold coins

// Dynamic properties utilizing the theme's current colors
val CyberSlate: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val CyberCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val CyberBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val TextWhite: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextGray: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

// Legacy templates placeholders
val Purple80 = NeonMint
val PurpleGrey80 = CyberCardDark
val Pink80 = CyberPink

val Purple40 = NeonPurple
val PurpleGrey40 = CyberBorderDark
val Pink40 = AccentOrange
