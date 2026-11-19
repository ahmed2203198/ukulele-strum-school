package com.ukulelestrum.school.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TropicalScheme = darkColorScheme(
    primary = Leaf, onPrimary = Forest, secondary = Lagoon, onSecondary = Mist,
    tertiary = Coral, onTertiary = Mist, background = Forest, onBackground = Mist,
    surface = Forest, onSurface = Mist, surfaceVariant = Lagoon, onSurfaceVariant = Mist,
    outline = Mist.copy(alpha = 0.18f), error = Coral, onError = Mist
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TropicalScheme, typography = StudioTypography, content = content)
}
