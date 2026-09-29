package com.aldemar.aquasample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aldemar.aquasample.data.repository.SampleRepository
import com.aldemar.aquasample.ui.screens.home.HomeScreen
import com.aldemar.aquasample.ui.screens.login.LoginScreen
import com.aldemar.aquasample.ui.screens.review.SupervisorReviewScreen
import com.aldemar.aquasample.ui.screens.sample.CountObservationsScreen
import com.aldemar.aquasample.ui.screens.sample.NewSampleScreen
import com.aldemar.aquasample.ui.screens.sample.PhotoCaptureScreen
import com.aldemar.aquasample.ui.screens.sample.SampleSummaryScreen
import com.aldemar.aquasample.ui.viewmodel.AuthViewModel
import com.aldemar.aquasample.ui.viewmodel.SampleCreateViewModel
import com.aldemar.aquasample.ui.viewmodel.SampleListViewModel
import com.aldemar.aquasample.ui.viewmodel.SupervisorReviewViewModel

/**
 * Grafo de Navegación Central (NavHost).
 *
 * Arquitectura de Navegación:
 * - Gestiona la transición entre pantallas mediante Jetpack Navigation Compose.
 * - Conecta los ViewModels a cada pantalla según su responsabilidad.
 * - Mantiene el estado del formulario de creación compartido a lo largo de los 4 pasos del wizard.
 * - Transfiere el ID de la muestra hacia la pantalla de revisión del supervisor.
 */
@Composable
fun AquaNavigation(
    repository: SampleRepository,
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()

    // Instancias de ViewModels vinculadas al ciclo de vida del NavHost
    val sampleListViewModel = remember { SampleListViewModel(repository) }
    val sampleCreateViewModel = remember { SampleCreateViewModel(repository) }
    val supervisorReviewViewModel = remember { SupervisorReviewViewModel(repository) }

    NavHost(
        navController = navController,
        startDestination = if (authUiState.isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        // ==========================================
        // 1. Pantalla de Login
        // ==========================================
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role, name ->
                    authViewModel.login(role, name)
                    sampleCreateViewModel.setOperator(name)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 2. Pantalla Principal: Historial y Filtros
        // ==========================================
        composable(Screen.Home.route) {
            HomeScreen(
                authUiState = authUiState,
                viewModel = sampleListViewModel,
                onNavigateToNewSample = {
                    sampleCreateViewModel.reset()
                    sampleCreateViewModel.setOperator(authUiState.currentUserName)
                    navController.navigate(Screen.NewSample.route)
                },
                onNavigateToReview = { sampleId ->
                    navController.navigate(Screen.SupervisorReview.createRoute(sampleId))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 3. Wizard Paso 1: Ubicación y Datos Base
        // ==========================================
        composable(Screen.NewSample.route) {
            NewSampleScreen(
                viewModel = sampleCreateViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPhoto = { navController.navigate(Screen.PhotoCapture.route) }
            )
        }

        // ==========================================
        // 4. Wizard Paso 2: Evidencia Fotográfica
        // ==========================================
        composable(Screen.PhotoCapture.route) {
            PhotoCaptureScreen(
                viewModel = sampleCreateViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCount = { navController.navigate(Screen.CountObservations.route) }
            )
        }

        // ==========================================
        // 5. Wizard Paso 3: Conteo Numérico y Observaciones
        // ==========================================
        composable(Screen.CountObservations.route) {
            CountObservationsScreen(
                viewModel = sampleCreateViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummary = { navController.navigate(Screen.SampleSummary.route) }
            )
        }

        // ==========================================
        // 6. Wizard Paso 4: Resumen y Persistencia Local
        // ==========================================
        composable(Screen.SampleSummary.route) {
            SampleSummaryScreen(
                viewModel = sampleCreateViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    // Al guardar exitosamente, volvemos al Home limpiando la pila del formulario
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 7. Módulo de Revisión y Dictamen (Supervisor)
        // ==========================================
        composable(
            route = Screen.SupervisorReview.route,
            arguments = listOf(navArgument("sampleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sampleId = backStackEntry.arguments?.getLong("sampleId") ?: 0L
            SupervisorReviewScreen(
                sampleId = sampleId,
                authUiState = authUiState,
                viewModel = supervisorReviewViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
