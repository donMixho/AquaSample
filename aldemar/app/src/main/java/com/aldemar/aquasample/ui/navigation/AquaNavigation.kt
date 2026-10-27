package com.aldemar.aquasample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.aldemar.aquasample.AquaSampleApp
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
    realRepository: SampleRepository,
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val app = LocalContext.current.applicationContext as AquaSampleApp
    val repository = if (authUiState.isDemo) app.demoRepository else realRepository
    val mode = if (authUiState.isDemo) "demo" else "real"
    val factory = remember(repository) {
        viewModelFactory {
            initializer { SampleListViewModel(repository, createSavedStateHandle()) }
            initializer { SampleCreateViewModel(repository, app.photoStorage, createSavedStateHandle()) }
            initializer { SupervisorReviewViewModel(repository) }
        }
    }
    // Activity-owned instances survive rotation and are cleared with their owner.
    val sampleListViewModel: SampleListViewModel = viewModel(key = "$mode-list", factory = factory)
    val sampleCreateViewModel: SampleCreateViewModel = viewModel(key = "$mode-create", factory = factory)

    androidx.compose.runtime.LaunchedEffect(authUiState.isLoggedIn, authUiState.isLoading) {
        if (!authUiState.isLoading) {
            val route = navController.currentDestination?.route
            if (authUiState.isLoggedIn && route == Screen.Login.route) {
                sampleCreateViewModel.setOperator(authUiState.currentUserName)
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
            } else if (!authUiState.isLoggedIn && route != null && route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) { popUpTo(navController.graph.id) { inclusive = true } }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // ==========================================
        // 1. Pantalla de Login
        // ==========================================
        composable(Screen.Login.route) {
            LoginScreen(
                authUiState = authUiState,
                authViewModel = authViewModel

            )
        }

        // ==========================================
        // 2. Pantalla Principal: Historial y Filtros
        // ==========================================
        composable(Screen.Home.route) {
            if (!authUiState.isLoggedIn) return@composable
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
                    sampleCreateViewModel.reset()
                    sampleListViewModel.clearFilters()
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
            if (!authUiState.isLoggedIn) return@composable
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
            if (!authUiState.isLoggedIn) return@composable
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
            if (!authUiState.isLoggedIn) return@composable
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
            if (!authUiState.isLoggedIn) return@composable
            SampleSummaryScreen(
                viewModel = sampleCreateViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    sampleListViewModel.showMessage("Muestra guardada. Disponible para revisión en este dispositivo.")
                    sampleListViewModel.clearFilters()
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
            if (!authUiState.isLoggedIn) return@composable
            val sampleId = backStackEntry.arguments?.getLong("sampleId") ?: 0L
            SupervisorReviewScreen(
                sampleId = sampleId,
                authUiState = authUiState,
                viewModel = viewModel<SupervisorReviewViewModel>(key = "$mode-review-$sampleId", factory = factory),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
