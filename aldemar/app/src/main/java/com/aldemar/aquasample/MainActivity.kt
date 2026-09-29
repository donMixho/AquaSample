package com.aldemar.aquasample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aldemar.aquasample.ui.navigation.AquaNavigation
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaSampleTheme

/**
 * Activity Principal de la aplicación (Single Activity Architecture).
 *
 * Enfoque moderno de Android:
 * - Toda la interfaz de usuario está construida con Jetpack Compose (100% código Kotlin declarativo).
 * - La navegación entre pantallas se gestiona dentro de un único NavHost (Single Activity).
 * - Se inyecta el repositorio desde la instancia global de Application (AquaSampleApp).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permite que la app dibuje de borde a borde (Edge-to-Edge) bajo las barras del sistema
        enableEdgeToEdge()

        // Obtenemos el repositorio singleton desde nuestra Application class
        val app = application as AquaSampleApp
        val repository = app.repository

        // Inicializamos el árbol de componentes de Jetpack Compose
        setContent {
            // Aplicamos el tema corporativo de ALDEMAR (Material Design 3)
            AquaSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AquaBackground
                ) {
                    // Contenedor de navegación principal con todas las rutas y pantallas
                    AquaNavigation(repository = repository)
                }
            }
        }
    }
}
