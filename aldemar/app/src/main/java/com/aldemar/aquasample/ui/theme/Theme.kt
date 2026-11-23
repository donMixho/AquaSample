package com.aldemar.aquasample.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.aldemar.aquasample.data.local.accessibility.AccessibilityState
import com.aldemar.aquasample.data.local.accessibility.AquaColors
import com.aldemar.aquasample.data.local.accessibility.getColorBlindnessPalette

/**
 * LocalProvider para exponer los colores dinámicos según el modo de accesibilidad.
 */
val LocalAquaColors = staticCompositionLocalOf { AquaColors() }

/**
 * Escala la tipografía base de Material 3 según el factor de tamaño de letra seleccionado.
 */
fun scaleTypography(base: Typography, fontScale: Float): Typography {
    if (fontScale == 1.0f) return base
    return Typography(
        headlineLarge = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize * fontScale, lineHeight = base.headlineLarge.lineHeight * fontScale),
        headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * fontScale, lineHeight = base.headlineMedium.lineHeight * fontScale),
        titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * fontScale, lineHeight = base.titleLarge.lineHeight * fontScale),
        titleMedium = base.titleMedium.copy(fontSize = base.titleMedium.fontSize * fontScale, lineHeight = base.titleMedium.lineHeight * fontScale),
        bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * fontScale, lineHeight = base.bodyLarge.lineHeight * fontScale),
        bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * fontScale, lineHeight = base.bodyMedium.lineHeight * fontScale),
        bodySmall = base.bodySmall.copy(fontSize = base.bodySmall.fontSize * fontScale, lineHeight = base.bodySmall.lineHeight * fontScale),
        labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * fontScale, lineHeight = base.labelLarge.lineHeight * fontScale),
        labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * fontScale, lineHeight = base.labelMedium.lineHeight * fontScale)
    )
}

/**
 * Tema principal de AquaSample con soporte completo para Daltonismo y Tamaño de Letra.
 */
@Composable
fun AquaSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilityState: AccessibilityState = AccessibilityState(),
    content: @Composable () -> Unit
) {
    val aquaColors = getColorBlindnessPalette(accessibilityState.colorBlindnessMode)

    val colorScheme = lightColorScheme(
        primary = aquaColors.primary,
        onPrimary = aquaColors.surface,
        primaryContainer = aquaColors.primary,
        onPrimaryContainer = aquaColors.surface,
        secondary = aquaColors.secondary,
        onSecondary = aquaColors.textPrimary,
        secondaryContainer = aquaColors.secondary,
        onSecondaryContainer = aquaColors.textPrimary,
        tertiary = aquaColors.statusValidated,
        onTertiary = aquaColors.surface,
        tertiaryContainer = aquaColors.statusValidatedBg,
        onTertiaryContainer = aquaColors.textPrimary,
        error = aquaColors.statusAlertDark,
        onError = aquaColors.surface,
        errorContainer = aquaColors.statusAlertBg,
        onErrorContainer = aquaColors.textPrimary,
        background = aquaColors.background,
        onBackground = aquaColors.textPrimary,
        surface = aquaColors.surface,
        onSurface = aquaColors.textPrimary,
        surfaceVariant = aquaColors.background,
        onSurfaceVariant = aquaColors.textSecondary,
        surfaceTint = aquaColors.primary,
        inverseSurface = aquaColors.textPrimary,
        inverseOnSurface = aquaColors.surface,
        inversePrimary = aquaColors.secondary,
        outline = aquaColors.outline,
        outlineVariant = aquaColors.outline,
        surfaceBright = aquaColors.surface,
        surfaceDim = aquaColors.background,
        surfaceContainerLowest = aquaColors.surface,
        surfaceContainerLow = aquaColors.surface,
        surfaceContainer = aquaColors.surface,
        surfaceContainerHigh = aquaColors.surface,
        surfaceContainerHighest = aquaColors.background
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = aquaColors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val scaledTypography = scaleTypography(Typography, accessibilityState.fontSizeScale.scale)

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * accessibilityState.fontSizeScale.scale
    )

    CompositionLocalProvider(
        LocalAquaColors provides aquaColors,
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}
