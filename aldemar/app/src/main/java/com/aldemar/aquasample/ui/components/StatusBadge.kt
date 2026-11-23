package com.aldemar.aquasample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.ui.theme.LocalAquaColors

/**
 * Badge o etiqueta visual para representar el estado de la muestra.
 *
 * Se adapta dinámicamente según la preferencia de accesibilidad y filtro de daltonismo activo.
 */
@Composable
fun StatusBadge(
    status: SampleStatus,
    modifier: Modifier = Modifier
) {
    val aquaColors = LocalAquaColors.current

    val (bgColor, textColor, borderColor) = when (status) {
        SampleStatus.VALIDADA -> Triple(aquaColors.statusValidatedBg, aquaColors.statusValidated, aquaColors.statusValidated.copy(alpha = 0.3f))
        SampleStatus.OBSERVADA -> Triple(aquaColors.statusAlertBg, aquaColors.statusAlertDark, aquaColors.statusAlertDark.copy(alpha = 0.4f))
        SampleStatus.PENDIENTE_REVISION, SampleStatus.CORREGIDA ->
            Triple(aquaColors.statusPendingBg, aquaColors.statusPending, aquaColors.statusPending.copy(alpha = 0.3f))
        SampleStatus.BORRADOR -> Triple(aquaColors.background, aquaColors.textSecondary, aquaColors.outline)
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(9999.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
