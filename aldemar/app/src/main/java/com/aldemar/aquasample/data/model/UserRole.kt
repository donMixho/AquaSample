package com.aldemar.aquasample.data.model

/**
 * Roles de usuario definidos en el caso de ALDEMAR SpA.
 *
 * - OPERADOR: Usuario que va en lancha/embarcación a las líneas de cultivo,
 *   toma la sección de cuerda, cuenta individuos y captura la evidencia fotográfica.
 * - SUPERVISOR: Profesional técnico que analiza los datos de muestreo, compara con históricos,
 *   y emite dictámenes (Validar u Observar).
 */
enum class UserRole(val label: String, val defaultUser: String) {
    OPERADOR("Operador de Muestreo", "Carlos Delgado"),
    SUPERVISOR("Supervisor Técnico", "Marcelo Crisóstomo")
}
