package com.flamematch.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// FlameMatch Colors
val FlameRed = Color(0xFFFF4458)
val FlameOrange = Color(0xFFFF7854)
val FlamePink = Color(0xFFFF6B9D)
val FlameGold = Color(0xFFFFD700)
val FlamePlatinum = Color(0xFFE5E4E2)

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCard = Color(0xFF2D2D2D)
val LightText = Color(0xFFFFFFFF)
val GrayText = Color(0xFFB0B0B0)

val GoldPremium = Color(0xFFFFD700)
val PlatinumPremium = Color(0xFFE5E4E2)

private val DarkColorScheme = darkColorScheme(
    primary = FlameRed,
    onPrimary = Color.White,
    secondary = FlameOrange,
    onSecondary = Color.White,
    tertiary = FlamePink,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = DarkCard,
    outline = GrayText
)

@Composable
fun FlameMatchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
