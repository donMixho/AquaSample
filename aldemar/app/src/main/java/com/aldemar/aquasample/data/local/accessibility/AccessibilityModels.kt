package com.aldemar.aquasample.data.local.accessibility

import androidx.compose.ui.graphics.Color
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.theme.StatusAlertBg
import com.aldemar.aquasample.ui.theme.StatusAlertDark
import com.aldemar.aquasample.ui.theme.StatusPending
import com.aldemar.aquasample.ui.theme.StatusPendingBg
import com.aldemar.aquasample.ui.theme.StatusValidated
import com.aldemar.aquasample.ui.theme.StatusValidatedBg

/**
 * Tipos de Daltonismo soportados por la aplicación.
 */
enum class ColorBlindnessMode(
    val label: String,
    val description: String
) {
    NONE("Sin daltonismo", "Paleta de colores oficial ALDEMAR"),
    DEUTERANOMALY("Deuteranomalía", "Percepción reducida del color verde"),
    PROTANOMALY("Protanomalía", "Percepción reducida del color rojo"),
    DEUTERANOPIA("Deuteranopia", "Ceguera al color verde"),
    PROTANOPIA("Protanopia", "Ceguera al color rojo"),
    TRITANOMALY("Tritanomalía", "Percepción reducida del color azul"),
    TRITANOPIA("Tritanopia", "Ceguera al color azul");

    companion object {
        fun fromName(name: String?): ColorBlindnessMode =
            entries.firstOrNull { it.name == name } ?: NONE
    }
}

/**
 * Escalas de tamaño de letra de la aplicación.
 */
enum class FontSizeScale(
    val label: String,
    val scale: Float
) {
    NORMAL("Normal (100%)", 1.0f),
    MEDIUM("Mediano (115%)", 1.15f),
    LARGE("Grande (130%)", 1.30f),
    EXTRA_LARGE("Extra Grande (145%)", 1.45f);

    companion object {
        fun fromName(name: String?): FontSizeScale =
            entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

/**
 * Estado inmutable de configuración de accesibilidad.
 */
data class AccessibilityState(
    val colorBlindnessMode: ColorBlindnessMode = ColorBlindnessMode.NONE,
    val fontSizeScale: FontSizeScale = FontSizeScale.NORMAL
)

/**
 * Paleta de colores dinámica adaptada según el modo de accesibilidad.
 */
data class AquaColors(
    val primary: Color = AquaPrimary,
    val secondary: Color = AquaSecondary,
    val background: Color = AquaBackground,
    val surface: Color = AquaSurface,
    val textPrimary: Color = AquaTextPrimary,
    val textSecondary: Color = AquaTextSecondary,
    val outline: Color = AquaOutline,
    val statusValidated: Color = StatusValidated,
    val statusValidatedBg: Color = StatusValidatedBg,
    val statusAlertDark: Color = StatusAlertDark,
    val statusAlertBg: Color = StatusAlertBg,
    val statusPending: Color = StatusPending,
    val statusPendingBg: Color = StatusPendingBg
)

/**
 * Genera la paleta de colores optimizada según el tipo de daltonismo seleccionado.
 */
fun getColorBlindnessPalette(mode: ColorBlindnessMode): AquaColors {
    return when (mode) {
        ColorBlindnessMode.NONE -> AquaColors()

        ColorBlindnessMode.DEUTERANOMALY -> AquaColors(
            primary = Color(0xFF005A9C),
            secondary = Color(0xFF0284C7),
            statusValidated = Color(0xFF0284C7), // Azul Ciel en lugar de verde
            statusValidatedBg = Color(0xFFE0F2FE),
            statusAlertDark = Color(0xFFD97706), // Ámbar cálido en lugar de naranja-rojo
            statusAlertBg = Color(0xFFFEF3C7)
        )

        ColorBlindnessMode.PROTANOMALY -> AquaColors(
            primary = Color(0xFF1E40AF),
            secondary = Color(0xFF3B82F6),
            statusValidated = Color(0xFF0369A1), // Teal marino
            statusValidatedBg = Color(0xFFE0F2FE),
            statusAlertDark = Color(0xFFB45309), // Dorado intenso
            statusAlertBg = Color(0xFFFEF3C7)
        )

        ColorBlindnessMode.DEUTERANOPIA -> AquaColors(
            primary = Color(0xFF0F172A), // Azul noche de alto contraste
            secondary = Color(0xFF2563EB),
            statusValidated = Color(0xFF0284C7), // Azul Cobalto
            statusValidatedBg = Color(0xFFE0F2FE),
            statusAlertDark = Color(0xFFC2410C), // Mosaico Ámbar-Marrón
            statusAlertBg = Color(0xFFFFEDD5)
        )

        ColorBlindnessMode.PROTANOPIA -> AquaColors(
            primary = Color(0xFF1E3A8A),
            secondary = Color(0xFF0EA5E9),
            statusValidated = Color(0xFF0D9488), // Teal Intenso
            statusValidatedBg = Color(0xFFCCFBF1),
            statusAlertDark = Color(0xFFA16207), // Dorado Bronce
            statusAlertBg = Color(0xFFFEF9C3)
        )

        ColorBlindnessMode.TRITANOMALY -> AquaColors(
            primary = Color(0xFF5B21B6), // Púrpura Profundo
            secondary = Color(0xFF8B5CF6),
            statusValidated = Color(0xFF047857), // Verde Esmeralda
            statusValidatedBg = Color(0xFFD1FAE5),
            statusAlertDark = Color(0xFFBE123C), // Rojo Carmesí
            statusAlertBg = Color(0xFFFFE4E6)
        )

        ColorBlindnessMode.TRITANOPIA -> AquaColors(
            primary = Color(0xFF831843), // Vino Magenta
            secondary = Color(0xFFDB2777),
            statusValidated = Color(0xFF059669), // Verde Menta
            statusValidatedBg = Color(0xFFD1FAE5),
            statusAlertDark = Color(0xFFB91C1C), // Rojo Vivo
            statusAlertBg = Color(0xFFFEE2E2)
        )
    }
}
