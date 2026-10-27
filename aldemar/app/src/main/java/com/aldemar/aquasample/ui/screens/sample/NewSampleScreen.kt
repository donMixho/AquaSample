package com.aldemar.aquasample.ui.screens.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.viewmodel.SampleCreateViewModel

/**
 * Pantalla 3: Asistente Paso 1 - Datos Base y Ubicación de Cultivo (NewSampleScreen).
 *
 * Basada en el prototipo: AquaSample_MVP_Stitch/MVP/aquamuestra_nueva_muestra/nueva_muestra.html
 *
 * Propósito:
 * - Registrar la jerarquía de trazabilidad: Centro de Cultivo, Tren y Línea.
 * - Captura la longitud del tramo muestreado en metros (generalmente 1.0 metro de cuerda).
 * - Asigna el operador responsable y fecha automática.
 * - Barra de progreso que indica que estamos en el 25% del proceso de toma de muestra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSampleScreen(
    viewModel: SampleCreateViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPhoto: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val centers by viewModel.centers.collectAsStateWithLifecycle()

    var centerDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Nueva Muestra (Paso 1/4)",
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
            // Barra de Progreso del Asistente (25%)
            LinearProgressIndicator(
                progress = { 0.25f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AquaSecondary,
                trackColor = AquaOutline.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AquaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ubicación del Cultivo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                    Text(
                        text = "Seleccione el centro y cuerda según bitácora de terreno.",
                        fontSize = 12.sp,
                        color = AquaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector desplegable de Centros de ALDEMAR
                    ExposedDropdownMenuBox(
                        expanded = centerDropdownExpanded,
                        onExpandedChange = { centerDropdownExpanded = !centerDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.centerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Centro de Cultivo *") },
                            isError = uiState.fieldErrors.containsKey("center"),
                            supportingText = { uiState.fieldErrors["center"]?.let { Text(it) } },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = centerDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaSecondary,
                                unfocusedBorderColor = AquaOutline
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = centerDropdownExpanded,
                            onDismissRequest = { centerDropdownExpanded = false }
                        ) {
                            val availableCenters = if (centers.isNotEmpty()) {
                                centers.map { it.name }.distinct()
                            } else {
                                listOf("Centro Huar", "Centro Chidhuapi", "Centro Calbuco", "Centro Quellón")
                            }

                            availableCenters.forEach { centerName ->
                                DropdownMenuItem(
                                    text = { Text(centerName) },
                                    onClick = {
                                        viewModel.updateCenter(centerName)
                                        centerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tren y Línea
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.trainNumber,
                            onValueChange = { viewModel.updateTrain(it) },
                            label = { Text("Tren / Módulo *") },
                            isError = uiState.fieldErrors.containsKey("train"),
                            supportingText = { uiState.fieldErrors["train"]?.let { Text(it) } },
                            placeholder = { Text("Ej: Tren 01") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaSecondary,
                                unfocusedBorderColor = AquaOutline
                            )
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                        OutlinedTextField(
                            value = uiState.lineNumber,
                            onValueChange = { viewModel.updateLine(it) },
                            label = { Text("Línea / Cuerda *") },
                            isError = uiState.fieldErrors.containsKey("line"),
                            supportingText = { uiState.fieldErrors["line"]?.let { Text(it) } },
                            placeholder = { Text("Ej: Línea 14") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaSecondary,
                                unfocusedBorderColor = AquaOutline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Longitud del tramo muestreado
                    OutlinedTextField(
                        value = uiState.sectionLengthMeters,
                        onValueChange = { viewModel.updateSectionLength(it) },
                        label = { Text("Longitud del Tramo Muestreado (Metros) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = uiState.fieldErrors.containsKey("length"),
                            supportingText = { uiState.fieldErrors["length"]?.let { Text(it) } },
                        placeholder = { Text("1.0") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaSecondary,
                            unfocusedBorderColor = AquaOutline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Operador Responsable
                    OutlinedTextField(
                        value = uiState.operatorName,
                        onValueChange = { viewModel.setOperator(it) },
                        label = { Text("Operador de Terreno *") },
                            isError = uiState.fieldErrors.containsKey("operator"),
                            supportingText = { uiState.fieldErrors["operator"]?.let { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaSecondary,
                            unfocusedBorderColor = AquaOutline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fecha y hora del registro
                    OutlinedTextField(
                        value = uiState.sampleDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha y Hora de Muestreo") },
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

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.fieldErrors.isNotEmpty()) {
                Text("Revisa los campos señalados para continuar.", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botón de avance al paso de fotografía
            Button(
                onClick = {
                    if (viewModel.validateStep1()) {
                        onNavigateToPhoto()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
            ) {
                Text(
                    text = "Siguiente: Evidencia Fotográfica ➔",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AquaSurface
                )
            }
        }
    }
}
