package com.aldemar.aquasample.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSurface

/**
 * Barra superior reutilizable de la aplicación (TopAppBar).
 *
 * Sigue el diseño de ALDEMAR:
 * - Fondo azul corporativo #00509E.
 * - Tipografía blanca en negrita.
 * - Botón de retroceso opcional con soporte de navegación.
 * - Espacio para acciones personalizadas (ej: cerrar sesión, cambiar rol).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = AquaSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = AquaSurface
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AquaPrimary,
            titleContentColor = AquaSurface,
            navigationIconContentColor = AquaSurface,
            actionIconContentColor = AquaSurface
        )
    )
}
