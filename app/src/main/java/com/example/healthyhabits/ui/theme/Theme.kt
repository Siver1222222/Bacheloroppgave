package com.example.healthyhabits.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DashboardDarkColorScheme = darkColorScheme(
    primary = NeonLime,
    secondary = Lavender,
    tertiary = SoftPink,
    background = DeepDark,
    surface = DarkGray,
    surfaceVariant = Color(0xFF20242C),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB8B8B8),
    outline = Color(0xFF3A3A3A),
    error = Color(0xFFFF6B6B),
    onError = Color.Black
)

// Light mode uses the same visual identity as dark mode, but with
// dedicated surfaces/text colors so every element keeps readable contrast.
private val DashboardLightColorScheme = lightColorScheme(
    primary = Color(0xFF5B6500),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9F6A5),
    onPrimaryContainer = Color(0xFF242900),
    secondary = Color(0xFF674F8F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADDFF),
    onSecondaryContainer = Color(0xFF24153F),
    tertiary = Color(0xFF8A496F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9EC),
    onTertiaryContainer = Color(0xFF321027),
    background = Color(0xFFF7F7F5),
    onBackground = Color(0xFF171717),
    surface = Color.White,
    onSurface = Color(0xFF171717),
    surfaceVariant = Color(0xFFECEDE8),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFF7A7D76),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun TreningsvaneTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DashboardDarkColorScheme else DashboardLightColorScheme,
        typography = AppTypography,
        content = content
    )
}
