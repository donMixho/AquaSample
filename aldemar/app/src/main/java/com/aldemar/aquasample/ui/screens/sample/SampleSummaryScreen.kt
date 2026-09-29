package com.aldemar.aquasample.ui.screens.sample

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.theme.StatusValidated
import com.aldemar.aquasample.ui.viewmodel.SampleCreateViewModel

/**
 * Pantalla 6: Asistente Paso 4 - Resumen y Confirmación Final (SampleSummaryScreen).
 *
 * Basada en el prototipo: AquaSample_MVP_Stitch/MVP/aquamuestra_resumen_de_muestra/resumen.html
 *
 * Propósito:
 * - Mostrar la ficha técnica consolidada antes del guardado definitivo en la base de datos Room.
 * - El operador puede revisar todos los datos: Centro, Tren, Línea, Tramo, Conteo, Notas y Foto.
 * - Al pulsar "Guardar y Enviar a Revisión", la muestra se persiste en SQLite con estado inicial "PENDIENTE".
 * - Progreso al 100% en verde.
 */
@Composable
fun SampleSummaryScreen(
    viewModel: SampleCreateViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Resumen de Muestra (Paso 4/4)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Barra de Progreso del Asistente Completa (100% en verde)
            LinearProgressIndicator(
                progress = { 1.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusValidated,
                trackColor = AquaOutline.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Resumen Técnico
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
                            text = "Ficha de Terreno",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaPrimary
                        )
                        StatusBadge(status = SampleStatus.PENDIENTE)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SummaryRow(label = "Centro de Cultivo", value = uiState.centerName)
                    SummaryRow(label = "Tren / Módulo", value = uiState.trainNumber)
                    SummaryRow(label = "Línea / Cuerda", value = uiState.lineNumber)
                    SummaryRow(label = "Tramo Muestreado", value = "${uiState.sectionLengthMeters} metro(s)")
                    SummaryRow(label = "Conteo Total", value = "${uiState.musselCount} choritos")
                    SummaryRow(label = "Operador", value = uiState.operatorName)
                    SummaryRow(label = "Fecha y Hora", value = uiState.sampleDate)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AquaOutline)

                    Text(
                        text = "Observaciones:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AquaTextPrimary
                    )
                    Text(
                        text = uiState.observations.ifBlank { "Sin observaciones adicionales." },
                        fontSize = 13.sp,
                        color = AquaTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Previsualización de la foto adjunta
                    if (uiState.photoPath != null) {
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
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AquaOutline, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uiState.photoPath,
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota sobre persistencia local offline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AquaSurface, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, AquaOutline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AquaSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "El registro quedará almacenado localmente en la base de datos de ALDEMAR y disponible para la revisión del supervisor.",
                    fontSize = 11.sp,
                    color = AquaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Confirmación y Guardado en SQLite
            Button(
                onClick = {
                    viewModel.saveSample {
                        viewModel.reset()
                        onSaveSuccess()
                    }
                },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = AquaSurface,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Guardar y Enviar a Revisión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AquaSurface
                    )
                }
            }
        }
    }
}

/**
 * Fila auxiliar para renderizar etiqueta y valor alineados.
 */
@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = AquaTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AquaTextPrimary)
    }
}
