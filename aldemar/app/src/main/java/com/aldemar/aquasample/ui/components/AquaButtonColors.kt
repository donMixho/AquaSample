package com.aldemar.aquasample.ui.components

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.aldemar.aquasample.ui.theme.AquaDisabledContainer
import com.aldemar.aquasample.ui.theme.AquaDisabledContent
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTonalContainer

/**
 * Colores de botones con contraste legible en TODOS los estados.
 *
 * Antes se usaba buttonColors(containerColor = X) sin definir los colores deshabilitados,
 * y Material dejaba texto gris claro sobre fondo gris claro (ilegible).
 */
object AquaButtonColors {

    /** Botón de acción principal: texto blanco sobre [container] (debe ser un color oscuro). */
    @Composable
    fun filled(container: Color = AquaPrimary): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = container,
        contentColor = AquaSurface,
        disabledContainerColor = AquaDisabledContainer,
        disabledContentColor = AquaDisabledContent
    )

    /**
     * Botón de acción principal mientras carga: mantiene el fondo de color
     * para que el indicador de progreso blanco siga viéndose.
     */
    @Composable
    fun filledKeepOnDisabled(container: Color = AquaPrimary): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = container,
        contentColor = AquaSurface,
        disabledContainerColor = container.copy(alpha = 0.8f),
        disabledContentColor = AquaSurface
    )

    /** Botón tonal (secundario): texto azul corporativo sobre azul muy claro. */
    @Composable
    fun tonal(): ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = AquaTonalContainer,
        contentColor = AquaPrimary,
        disabledContainerColor = AquaDisabledContainer,
        disabledContentColor = AquaDisabledContent
    )
}
