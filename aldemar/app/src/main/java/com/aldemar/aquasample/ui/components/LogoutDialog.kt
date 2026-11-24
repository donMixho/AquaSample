package com.aldemar.aquasample.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AquaSurface,
        title = {
            Text(
                text = "Cerrar sesión",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AquaPrimary
            )
        },
        text = {
            Text(
                text = "¿Quieres cerrar sesión?",
                fontSize = 15.sp,
                color = AquaTextPrimary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sí", fontWeight = FontWeight.Bold, color = AquaPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No", color = AquaTextPrimary)
            }
        }
    )
}
