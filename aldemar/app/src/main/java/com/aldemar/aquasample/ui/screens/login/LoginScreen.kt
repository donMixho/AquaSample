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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Pantalla 1: Inicio de Sesión y Selección de Perfil (LoginScreen).
 *
 * Basada en el prototipo: AquaSample_MVP_Stitch/MVP/aquamuestra_login/login.html
 *
 * Propósito:
 * - Permite al evaluador o usuario ingresar con perfil de "Operador de Muestreo" o "Supervisor Técnico".
 * - Demuestra la adaptabilidad de la app según el rol.
 * - Incluye logotipo oficial de ALDEMAR y notas de funcionamiento offline.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole, String) -> Unit
) {
    // Estado local para el rol seleccionado y el nombre del profesional
    var selectedRole by remember { mutableStateOf(UserRole.OPERADOR) }
    var userName by remember { mutableStateOf(UserRole.OPERADOR.defaultUser) }

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
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Seleccione su Rol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaTextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de Roles mediante Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRole == UserRole.OPERADOR,
                        onClick = {
                            selectedRole = UserRole.OPERADOR
                            userName = UserRole.OPERADOR.defaultUser
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
                            userName = UserRole.SUPERVISOR.defaultUser
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

                // Campo para personalizar el nombre del operario o supervisor
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nombre del Profesional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaSecondary,
                        focusedLabelColor = AquaPrimary,
                        unfocusedBorderColor = AquaOutline
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal de acceso a la app
                Button(
                    onClick = { onLoginSuccess(selectedRole, userName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                ) {
                    Text(
                        text = "Ingresar a Terreno",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "✓ Persistencia Offline Habilitada (SQLite)",
                    fontSize = 11.sp,
                    color = AquaTextSecondary
                )
            }
        }
    }
}
