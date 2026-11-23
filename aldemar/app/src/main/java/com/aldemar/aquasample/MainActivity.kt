package com.aldemar.aquasample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aldemar.aquasample.ui.navigation.AquaNavigation
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaSampleTheme

/**
 * Activity Principal de la aplicación (Single Activity Architecture).
 *
 * Enfoque moderno de Android:
 * - Toda la interfaz de usuario está construida con Jetpack Compose (100% código Kotlin declarativo).
 * - La navegación entre pantallas se gestiona dentro de un único NavHost (Single Activity).
 * - Se inyecta el repositorio y las preferencias de accesibilidad desde AquaSampleApp.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AquaSampleApp
        val repository = app.repository
        val accessibilityPrefs = app.accessibilityPreferences

        setContent {
            val accessibilityState by accessibilityPrefs.state.collectAsStateWithLifecycle()

            AquaSampleTheme(accessibilityState = accessibilityState) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AquaBackground
                ) {
                    AquaNavigation(realRepository = repository)
                }
            }
        }
    }
}
