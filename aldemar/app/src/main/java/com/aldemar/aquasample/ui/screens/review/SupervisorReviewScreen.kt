package com.aldemar.aquasample.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.components.StatusBadge
import com.aldemar.aquasample.ui.screens.sample.SummaryRow
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.theme.StatusAlert
import com.aldemar.aquasample.ui.theme.StatusValidated
import com.aldemar.aquasample.ui.viewmodel.AuthUiState
import com.aldemar.aquasample.ui.viewmodel.SupervisorReviewViewModel

/**
 * Pantalla 7: Módulo de Revisión y Dictamen Técnico del Supervisor (SupervisorReviewScreen).
 *
 * Basada en el prototipo: AquaSample_MVP_Stitch/MVP/aquamuestra_revisi_n_supervisor/revision.html
 *
 * Propósito en el flujo del negocio:
 * - El Supervisor Técnico revisa las muestras tomadas por los operadores en terreno.
 * - Inspecciona el tramo, conteo, fecha, notas y la evidencia fotográfica ampliada.
 * - Puede ingresar un comentario o instrucción técnica y ejecutar dos acciones inmediatas:
 *   1. VALIDAR: Marca el registro en verde con estado VALIDADO.
 *   2. OBSERVAR: Marca el registro en naranja con estado OBSERVADO para requerir seguimiento.
 * - Los cambios quedan persistidos en la base de datos Room SQLite y se reflejan al instante en el Historial.
 */
@Composable
fun SupervisorReviewScreen(
    sampleId: Long,
    authUiState: AuthUiState,
    viewModel: SupervisorReviewViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Carga asíncrona de la muestra al ingresar a la pantalla
    LaunchedEffect(sampleId) {
        viewModel.loadSample(sampleId)
    }

    val sample = uiState.sample

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Revisión Técnica de Muestra",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        if (sample == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando muestra...", color = AquaTextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Tarjeta de Detalle de Muestra
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AquaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sample.code,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaPrimary
                            )
                            StatusBadge(status = SampleStatus.fromString(sample.status))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SummaryRow(label = "Centro de Cultivo", value = sample.centerName)
                        SummaryRow(label = "Tren / Módulo", value = sample.trainNumber)
                        SummaryRow(label = "Línea / Cuerda", value = sample.lineNumber)
                        SummaryRow(label = "Tramo Medido", value = "${sample.sectionLengthMeters} metro(s)")
                        SummaryRow(label = "Conteo Realizado", value = "${sample.musselCount} choritos")
                        SummaryRow(label = "Operador de Terreno", value = sample.operatorName)
                        SummaryRow(label = "Fecha de Registro", value = sample.sampleDate)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AquaOutline)

                        Text(
                            text = "Observaciones de Terreno:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaTextPrimary
                        )
                        Text(
                            text = sample.observations,
                            fontSize = 13.sp,
                            color = AquaTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        // Foto de evidencia si fue adjuntada
                        if (sample.photoPath != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Evidencia Fotográfica:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AquaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, AquaOutline, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = sample.photoPath,
                                    contentDescription = "Evidencia fotográfica",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Panel de Decisión y Dictamen del Supervisor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AquaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Evaluación del Supervisor Técnico",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaPrimary
                        )
                        Text(
                            text = "Ingrese su dictamen técnico y seleccione la acción correspondiente.",
                            fontSize = 12.sp,
                            color = AquaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo de comentario o instrucción técnica
                        OutlinedTextField(
                            value = uiState.supervisorComment,
                            onValueChange = { viewModel.updateComment(it) },
                            label = { Text("Comentario o Instrucción Técnica") },
                            placeholder = { Text("Ej: Conteo consistente con la curva de crecimiento...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaSecondary,
                                unfocusedBorderColor = AquaOutline
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de Acción: Validar u Observar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Acción: Observar (Naranja)
                            Button(
                                onClick = {
                                    viewModel.applyReview(
                                        newStatus = SampleStatus.OBSERVADO,
                                        supervisorName = authUiState.currentUserName,
                                        onSuccess = onNavigateBack
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAlert)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Observar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaSurface
                                )
                            }

                            // Acción: Validar (Verde)
                            Button(
                                onClick = {
                                    viewModel.applyReview(
                                        newStatus = SampleStatus.VALIDADO,
                                        supervisorName = authUiState.currentUserName,
                                        onSuccess = onNavigateBack
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusValidated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Validar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
