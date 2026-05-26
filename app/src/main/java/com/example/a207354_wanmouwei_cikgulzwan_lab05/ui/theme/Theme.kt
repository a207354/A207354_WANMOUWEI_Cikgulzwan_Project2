package com.example.a207354_wanmouwei_cikgulzwan_lab05.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = SpotifyGreen,
    background = BackgroundColor,
    surface = SurfaceColor,

    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun A207354_WANMOUWEI_Cikgulzwan_Lab05Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}