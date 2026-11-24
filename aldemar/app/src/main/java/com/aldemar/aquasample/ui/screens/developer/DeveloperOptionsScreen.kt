package com.aldemar.aquasample.ui.screens.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aldemar.aquasample.AquaSampleApp
import com.aldemar.aquasample.data.local.accessibility.ColorBlindnessMode
import com.aldemar.aquasample.data.local.accessibility.FontSizeScale
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.theme.LocalAquaColors
import com.aldemar.aquasample.ui.theme.StatusAlertDark
import com.aldemar.aquasample.ui.theme.StatusValidated
import com.aldemar.aquasample.ui.viewmodel.AuthViewModel

/**
 * Pantalla de Opciones de Desarrollador y Accesibilidad.
 *
 * Incluye:
 * - Ajuste persistente de tamaño de fuente (Normal, Mediano, Grande, Extra Grande).
 * - Selección persistente del modo de daltonismo (Deuteranomalía, Protanomalía, Deuteranopia, Protanopia, Tritanomalía, Tritanopia).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperOptionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as AquaSampleApp
    val accessibilityPrefs = app.accessibilityPreferences
    val accessibilityState by accessibilityPrefs.state.collectAsStateWithLifecycle()
    val activeAquaColors = LocalAquaColors.current

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Accesibilidad",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = AquaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = null,
                        tint = activeAquaColors.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Accesibilidad",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeAquaColors.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ajusta el tamaño del texto y la paleta de colores según tus necesidades.",
                        fontSize = 13.sp,
                        color = AquaTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ==========================================
                    // SECCIÓN DE ACCESIBILIDAD VISUAL
                    // ==========================================
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Accessibility,
                                contentDescription = null,
                                tint = activeAquaColors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Configuración de Accesibilidad",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAquaColors.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Tamaño de Fuente
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = AquaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Tamaño de la Fuente (Letras)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AquaTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FontSizeScale.entries.forEach { scaleOption ->
                                FilterChip(
                                    selected = accessibilityState.fontSizeScale == scaleOption,
                                    onClick = { accessibilityPrefs.setFontSizeScale(scaleOption) },
                                    label = { Text(scaleOption.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = activeAquaColors.primary,
                                        selectedLabelColor = AquaSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 2. Modos de Daltonismo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = AquaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Filtro para Daltonismo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AquaTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ColorBlindnessMode.entries.forEach { modeOption ->
                                FilterChip(
                                    selected = accessibilityState.colorBlindnessMode == modeOption,
                                    onClick = { accessibilityPrefs.setColorBlindnessMode(modeOption) },
                                    label = { Text(modeOption.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = activeAquaColors.primary,
                                        selectedLabelColor = AquaSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Vista Previa de Colores Activos
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = activeAquaColors.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Vista Previa de Colores (${accessibilityState.colorBlindnessMode.label})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaTextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Muestra Primario
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(activeAquaColors.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Primario",
                                            color = AquaSurface,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Muestra Validado
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(activeAquaColors.statusValidated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Validado",
                                            color = AquaSurface,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Muestra Alerta / Observado
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(activeAquaColors.statusAlertDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Observado",
                                            color = AquaSurface,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                }
            }
        }
    }
}
