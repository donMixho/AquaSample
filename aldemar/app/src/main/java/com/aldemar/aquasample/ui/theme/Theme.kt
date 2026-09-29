package com.aldemar.aquasample.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores para Material Design 3 en modo claro (Light Color Scheme).
 * Priorizado para operaciones diurnas en terreno marino (embarcaciones y pontones).
 */
private val LightColorScheme = lightColorScheme(
    primary = AquaPrimary,
    onPrimary = AquaSurface,
    primaryContainer = AquaPrimaryVariant,
    onPrimaryContainer = AquaSurface,
    secondary = AquaSecondary,
    onSecondary = AquaSurface,
    background = AquaBackground,
    onBackground = AquaTextPrimary,
    surface = AquaSurface,
    onSurface = AquaTextPrimary,
    surfaceVariant = AquaBackground,
    onSurfaceVariant = AquaTextSecondary,
    outline = AquaOutline
)

/**
 * Tema principal de AquaSample.
 * Configura la barra de estado del sistema (StatusBar) en color Azul ALDEMAR (#00509E).
 */
@Composable
fun AquaSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AquaPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
