package com.example.planttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    secondary = SoftSage,
    tertiary = EarthyBrown,
    background = CreamyWhite,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onSecondary = EarthyBrown,
    onTertiary = SurfaceWhite,
    onBackground = EarthyBrown,
    onSurface = EarthyBrown,
    error = ErrorRed,
    secondaryContainer = SoftSage.copy(alpha = 0.1f),
    onSecondaryContainer = LeafGreen
)

@Composable
fun PlantTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
