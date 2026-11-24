package com.aldemar.aquasample.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.ui.components.AquaButtonColors
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.theme.*
import com.aldemar.aquasample.ui.viewmodel.AuthViewModel

@Composable
fun DemoLoginScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isVerified by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Modo Demostración",
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
                    Text(
                        text = "Acceso a Modo Demostración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingrese la contraseña de demostración para acceder a los perfiles de prueba.",
                        fontSize = 13.sp,
                        color = AquaTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isVerified) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Contraseña:") },
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

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (password == "@ldermar2026!") {
                                    isVerified = true
                                    errorMessage = null
                                } else {
                                    errorMessage = "Contraseña incorrecta."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = AquaButtonColors.filled()
                        ) {
                            Text(
                                text = "Verificar Contraseña",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaSurface
                            )
                        }
                    } else {
                        Text(
                            text = "¡Contraseña verificada con éxito!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusValidated
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Seleccione el perfil para iniciar sesión:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        UserRole.entries.forEach { role ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    authViewModel.loginDemo(
                                        role = role,
                                        customName = role.defaultUser
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = AquaButtonColors.filled()
                            ) {
                                Text(
                                    text = "Iniciar como ${role.shortLabel} (${role.label})",
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
}
