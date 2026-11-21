package com.aldemar.aquasample.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de color oficiales de ALDEMAR SpA (Corporate Precision System).
 *
 * Definidos en: AquaSample_MVP_Stitch/MVP/corporate_precision_system/DESIGN.md
 *
 * Criterio de diseño:
 * - AquaPrimary (#00509E): Azul marino de alta autoridad para barras superiores y botones principales de acción (CTA).
 * - AquaSecondary (#4DA6FF): Azul celeste para elementos interactivos, indicadores de progreso e iconos activos.
 * - AquaBackground (#F5F5F5): Fondo neutro claro con alto contraste para lectura bajo luz solar en el mar.
 * - AquaSurface (#FFFFFF): Blanco puro para tarjetas elevadas (Cards) y formularios.
 * - Estados semánticos: Verde (#2E7D32) para validado y Naranja (#FF6D2D) para observaciones/alertas.
 */
val AquaPrimary = Color(0xFF00509E)
val AquaPrimaryVariant = Color(0xFF003D7A)
val AquaSecondary = Color(0xFF4DA6FF)
val AquaBackground = Color(0xFFF5F5F5)
val AquaSurface = Color(0xFFFFFFFF)
val AquaTextPrimary = Color(0xFF212121)
val AquaTextSecondary = Color(0xFF616161)
val AquaTextDisabled = Color(0xFF9E9E9E)
val AquaOutline = Color(0xFFD1D5DB)

// Colores de Estados de Supervisión y Trazabilidad
val StatusValidated = Color(0xFF2E7D32)
val StatusValidatedBg = Color(0xFFE8F5E9)
val StatusAlert = Color(0xFFFF6D2D)
val StatusAlertBg = Color(0xFFFFF3E0)

// Naranja oscuro para TEXTO y botones de alerta (contraste >= 4.5:1 sobre blanco y sobre StatusAlertBg).
// StatusAlert (#FF6D2D) solo da 2.8:1, por eso se reserva para iconos y bordes decorativos.
val StatusAlertDark = Color(0xFFB84300)

// Fondo de botones secundarios/tonales: azul claro con texto AquaPrimary (contraste > 6:1).
val AquaTonalContainer = Color(0xFFD9EAFB)

// Botones deshabilitados: gris medio con texto gris oscuro (legible, no gris sobre gris).
val AquaDisabledContainer = Color(0xFFCFD4DA)
val AquaDisabledContent = Color(0xFF4B5563)

// Contenedores claros (azul muy suave) con texto oscuro #212121.
val AquaSoftContainer = Color(0xFFE3F2FD)
val StatusPending = Color(0xFF00509E)
val StatusPendingBg = Color(0xFFEBF5FF)
