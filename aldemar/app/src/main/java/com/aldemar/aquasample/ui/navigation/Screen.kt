package com.aldemar.aquasample.ui.navigation

/**
 * Rutas de navegación de AquaSample.
 *
 * Utiliza una 'sealed class' para garantizar que las rutas sean seguras y tipadas en tiempo de compilación.
 *
 * Flujo:
 * - Login: Pantalla de bienvenida y selección de rol.
 * - Home: Listado histórico con filtros y búsqueda.
 * - Asistente de Nueva Muestra (4 Pasos):
 *     1. NewSample (Ubicación y tramo)
 *     2. PhotoCapture (Evidencia fotográfica)
 *     3. CountObservations (Conteo numérico y notas)
 *     4. SampleSummary (Resumen y guardado en SQLite)
 * - SupervisorReview: Revisión técnica parametrizada con el ID de la muestra.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object NewSample : Screen("new_sample")
    data object PhotoCapture : Screen("photo_capture")
    data object CountObservations : Screen("count_observations")
    data object SampleSummary : Screen("sample_summary")
    data object SupervisorReview : Screen("supervisor_review/{sampleId}") {
        fun createRoute(sampleId: Long): String = "supervisor_review/$sampleId"
    }
}
