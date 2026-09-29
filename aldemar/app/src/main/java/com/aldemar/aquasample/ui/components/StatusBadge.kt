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
import com.aldemar.aquasample.ui.theme.StatusAlert
import com.aldemar.aquasample.ui.theme.StatusAlertBg
import com.aldemar.aquasample.ui.theme.StatusPending
import com.aldemar.aquasample.ui.theme.StatusPendingBg
import com.aldemar.aquasample.ui.theme.StatusValidated
import com.aldemar.aquasample.ui.theme.StatusValidatedBg

/**
 * Badge o etiqueta visual para representar el estado de la muestra.
 *
 * Aplica la forma de píldora (Pill shape 9999.dp) y los colores semánticos definidos:
 * - VALIDADO: Fondo verde claro (#E8F5E9) con texto verde oscuro (#2E7D32).
 * - OBSERVADO: Fondo naranja claro (#FFF3E0) con texto naranja (#FF6D2D).
 * - PENDIENTE: Fondo azul claro (#EBF5FF) con texto azul (#00509E).
 */
@Composable
fun StatusBadge(
    status: SampleStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        SampleStatus.VALIDADO -> Triple(StatusValidatedBg, StatusValidated, StatusValidated.copy(alpha = 0.3f))
        SampleStatus.OBSERVADO -> Triple(StatusAlertBg, StatusAlert, StatusAlert.copy(alpha = 0.4f))
        SampleStatus.PENDIENTE -> Triple(StatusPendingBg, StatusPending, StatusPending.copy(alpha = 0.3f))
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
