package com.aldemar.aquasample.ui.screens.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.R
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.viewmodel.AuthUiState
import com.aldemar.aquasample.ui.viewmodel.AuthViewModel

/**
 * Pantalla de Inicio de Sesión y Verificación por medio de Firebase Authentication.
 *
 * Incluye:
 * - Autenticación remota mediante Firebase Auth (Email/Contraseña).
 * - Selección de perfil (Operador vs. Supervisor) para control de acceso por rol.
 * - Registro de nuevos usuarios y modo offline de demostración.
 */
@Composable
fun LoginScreen(
    authUiState: AuthUiState,
    authViewModel: AuthViewModel,
    onLoginSuccess: (UserRole, String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.OPERADOR) }
    var userName by remember { mutableStateOf(UserRole.OPERADOR.defaultUser) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Registro

    // Diálogo de Error de Firebase
    if (authUiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            title = {
                Text(
                    text = "Error de Autenticación",
                    fontWeight = FontWeight.Bold,
                    color = AquaPrimary
                )
            },
            text = {
                Text(
                    text = authUiState.errorMessage,
                    fontSize = 14.sp,
                    color = AquaTextPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearError() }) {
                    Text("Aceptar", color = AquaPrimary)
                }
            },
            containerColor = AquaSurface
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AquaBackground)
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
                // Logotipo oficial de ALDEMAR SpA
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo ALDEMAR",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AquaSample",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaPrimary
                )

                Text(
                    text = "ALDEMAR SpA · Monitoreo y Muestreo",
                    fontSize = 13.sp,
                    color = AquaTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Pestañas Iniciar Sesión / Registrarse
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AquaSurface,
                    contentColor = AquaPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AquaPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Iniciar Sesión",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) AquaPrimary else AquaTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Registrarse",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) AquaPrimary else AquaTextSecondary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Roles mediante Chips
                Text(
                    text = "Seleccione su Rol Corporativo",
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
                        selected = selectedRole == UserRole.OPERADOR,
                        onClick = {
                            selectedRole = UserRole.OPERADOR
                            if (userName.isBlank() || userName == UserRole.SUPERVISOR.defaultUser) {
                                userName = UserRole.OPERADOR.defaultUser
                            }
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
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = AquaSurface,
                            selectedLeadingIconColor = AquaSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedRole == UserRole.SUPERVISOR,
                        onClick = {
                            selectedRole = UserRole.SUPERVISOR
                            if (userName.isBlank() || userName == UserRole.OPERADOR.defaultUser) {
                                userName = UserRole.SUPERVISOR.defaultUser
                            }
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
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = AquaSurface,
                            selectedLeadingIconColor = AquaSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Correo Electrónico (Firebase)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico (Firebase)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AquaPrimary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaSecondary,
                        focusedLabelColor = AquaPrimary,
                        unfocusedBorderColor = AquaOutline
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña (Firebase)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AquaPrimary
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaSecondary,
                        focusedLabelColor = AquaPrimary,
                        unfocusedBorderColor = AquaOutline
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Nombre del Profesional
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nombre del Profesional") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AquaPrimary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaSecondary,
                        focusedLabelColor = AquaPrimary,
                        unfocusedBorderColor = AquaOutline
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón principal (Iniciar Sesión o Registrarse en Firebase)
                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            authViewModel.loginWithFirebase(
                                email = email,
                                pass = password,
                                role = selectedRole,
                                customName = userName,
                                onSuccess = onLoginSuccess
                            )
                        } else {
                            authViewModel.registerWithFirebase(
                                email = email,
                                pass = password,
                                role = selectedRole,
                                customName = userName,
                                onSuccess = onLoginSuccess
                            )
                        }
                    },
                    enabled = !authUiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                ) {
                    if (authUiState.isLoading) {
                        CircularProgressIndicator(
                            color = AquaSurface,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (selectedTab == 0) "Verificar e Ingresar con Firebase" else "Crear Cuenta Firebase",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón alternativo para demostración offline sin requerir credenciales Firebase
                OutlinedButton(
                    onClick = {
                        authViewModel.loginDemo(
                            role = selectedRole,
                            customName = userName,
                            onSuccess = onLoginSuccess
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Ingresar en Modo Demostración (Offline)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AquaPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🔒 Autenticación Firebase Verificada",
                        fontSize = 11.sp,
                        color = AquaTextSecondary
                    )
                }
            }
        }
    }
}
