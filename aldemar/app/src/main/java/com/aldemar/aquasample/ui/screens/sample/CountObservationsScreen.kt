package com.aldemar.aquasample.ui.screens.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaPrimaryVariant
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.viewmodel.SampleCreateViewModel

/**
 * Pantalla 5: Asistente Paso 3 - Conteo Numérico y Observaciones (CountObservationsScreen).
 *
 * Basada en el prototipo: AquaSample_MVP_Stitch/MVP/aquamuestra_conteo_y_observaciones/conteo.html
 *
 * Justificación de Diseño y Ergonomía de Terreno:
 * - En los centros de cultivo marinos, los operadores suelen llevar guantes de trabajo húmedos
 *   y estar sobre cubiertas con movimiento.
 * - Por eso se diseñó un contador visual con botones táctiles grandes de ajuste rápido:
 *   (+1, +10, +50, -1, -10), además de un campo numérico directo para mayor flexibilidad.
 * - Incluye un área de texto libre para observaciones técnicas (calibre, desprendimiento, presencia de fouling).
 */
@Composable
fun CountObservationsScreen(
    viewModel: SampleCreateViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Conteo y Notas (Paso 3/4)",
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
            // Barra de Progreso del Asistente (75%)
            LinearProgressIndicator(
                progress = { 0.75f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AquaSecondary,
                trackColor = AquaOutline.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Conteo de Individuos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AquaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Conteo de Choritos en Tramo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Individuos contabilizados en la sección de ${uiState.sectionLengthMeters} metro(s).",
                        fontSize = 12.sp,
                        color = AquaTextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Visor de número grande de alta visibilidad
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AquaBackground, shape = RoundedCornerShape(12.dp))
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.musselCount}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaPrimary
                            )
                            Text(
                                text = "individuos",
                                fontSize = 13.sp,
                                color = AquaTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones ergonómicos de ajuste rápido
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.incrementCount(-10) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("-10", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.incrementCount(-1) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos 1")
                        }

                        Button(
                            onClick = { viewModel.incrementCount(1) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más 1")
                        }

                        Button(
                            onClick = { viewModel.incrementCount(10) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                        ) {
                            Text("+10", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.incrementCount(50) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimaryVariant)
                        ) {
                            Text("+50", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Entrada de texto alternativa para valores grandes
                    OutlinedTextField(
                        value = if (uiState.musselCount == 0) "" else uiState.musselCount.toString(),
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 0
                            viewModel.setCount(parsed)
                        },
                        label = { Text("O ingrese número exacto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaSecondary,
                            unfocusedBorderColor = AquaOutline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Observaciones de Terreno
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AquaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Observaciones de Terreno",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                    Text(
                        text = "Calibre visual, densidad, presencia de fouling/algas o depredadores.",
                        fontSize = 12.sp,
                        color = AquaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.observations,
                        onValueChange = { viewModel.updateObservations(it) },
                        placeholder = { Text("Ej: Buena fijación de biso, calibre uniforme ~45mm, baja presencia de chorito picoroco...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaSecondary,
                            unfocusedBorderColor = AquaOutline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón hacia el paso final de confirmación
            Button(
                onClick = onNavigateToSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
            ) {
                Text(
                    text = "Siguiente: Resumen de Muestra ➔",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AquaSurface
                )
            }
        }
    }
}
