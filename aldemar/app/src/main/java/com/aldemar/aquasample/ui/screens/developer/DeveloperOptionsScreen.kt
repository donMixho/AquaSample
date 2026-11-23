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

private const val DEVELOPER_PASSWORD = "@ldemar2026!"

/**
 * Pantalla de Opciones de Desarrollador y Accesibilidad.
 *
 * Incluye:
 * - Autenticación por clave ("@ldemar2026!") para el panel de desarrollador.
 * - Ajuste persistente de tamaño de fuente (Normal, Mediano, Grande, Extra Grande).
 * - Selección persistente del modo de daltonismo (Deuteranomalía, Protanomalía, Deuteranopia, Protanopia, Tritanomalía, Tritanopia).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperOptionsScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as AquaSampleApp
    val accessibilityPrefs = app.accessibilityPreferences
    val accessibilityState by accessibilityPrefs.state.collectAsStateWithLifecycle()
    val activeAquaColors = LocalAquaColors.current

    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Configuración del perfil demo
    var selectedRole by remember { mutableStateOf(UserRole.OPERATOR) }
    var customName by remember { mutableStateOf(UserRole.OPERATOR.defaultUser) }

    fun verifyPassword() {
        if (passwordInput == DEVELOPER_PASSWORD) {
            isUnlocked = true
            errorMessage = null
        } else {
            errorMessage = "Contraseña de desarrollador incorrecta."
        }
    }

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Opciones de Desarrollador",
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
                        imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = if (isUnlocked) activeAquaColors.statusValidated else activeAquaColors.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isUnlocked) "Panel de Desarrollador" else "Acceso Restringido",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeAquaColors.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isUnlocked)
                            "Acceso concedido a las funciones de depuración, accesibilidad y modo demostración offline."
                        else
                            "Ingrese la contraseña corporativa de desarrollador para acceder.",
                        fontSize = 13.sp,
                        color = AquaTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isUnlocked) {
                        // Campo de Contraseña
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Contraseña de Desarrollador") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = activeAquaColors.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar u ocultar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            isError = errorMessage != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { verifyPassword() }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaSecondary,
                                focusedLabelColor = activeAquaColors.primary,
                                unfocusedBorderColor = AquaOutline,
                                errorBorderColor = StatusAlertDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = StatusAlertDark,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { verifyPassword() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = activeAquaColors.primary)
                        ) {
                            Text(
                                text = "Desbloquear Opciones",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaSurface
                            )
                        }
                    } else {
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

                        Spacer(modifier = Modifier.height(28.dp))

                        // ==========================================
                        // SECCIÓN MODO DEMOSTRACIÓN
                        // ==========================================
                        Text(
                            text = "Perfil para Demostración Offline",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaTextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedRole == UserRole.OPERATOR,
                                onClick = {
                                    selectedRole = UserRole.OPERATOR
                                    customName = UserRole.OPERATOR.defaultUser
                                },
                                label = { Text("Operador") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = activeAquaColors.primary,
                                    selectedLabelColor = AquaSurface,
                                    selectedLeadingIconColor = AquaSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = selectedRole == UserRole.SUPERVISOR,
                                onClick = {
                                    selectedRole = UserRole.SUPERVISOR
                                    customName = UserRole.SUPERVISOR.defaultUser
                                },
                                label = { Text("Supervisor") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SupervisorAccount,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = activeAquaColors.primary,
                                    selectedLabelColor = AquaSurface,
                                    selectedLeadingIconColor = AquaSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nombre del Profesional Demo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaSecondary,
                                focusedLabelColor = activeAquaColors.primary,
                                unfocusedBorderColor = AquaOutline
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de Demostración Offline
                        Button(
                            onClick = {
                                authViewModel.loginDemo(
                                    role = selectedRole,
                                    customName = customName
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = activeAquaColors.primary)
                        ) {
                            Text(
                                text = "Ingresar en Modo Demostración (Offline)",
                                fontSize = 14.sp,
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
