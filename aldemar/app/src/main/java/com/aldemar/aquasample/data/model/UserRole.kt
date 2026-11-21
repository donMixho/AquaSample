package com.aldemar.aquasample.data.model

/**
 * Roles de usuario del caso ALDEMAR SpA y sus permisos.
 *
 * - OPERATOR: va a terreno, crea muestras (foto + conteo). No revisa.
 * - SUPERVISOR: revisa y emite dictamen; también consulta, ve dashboard y exporta.
 * - ANALYST: solo lectura analítica: historial, dashboard y exportación.
 *   No crea muestras ni aprueba/observa revisiones.
 */
enum class UserRole(
    val label: String,
    val defaultUser: String,
    val canCreateSamples: Boolean = false,
    val canReview: Boolean = false,
    val canViewHistory: Boolean = true,
    val canViewDashboard: Boolean = false,
    val canExport: Boolean = false
) {
    OPERATOR("Operador de Muestreo", "Carlos Delgado", canCreateSamples = true),
    SUPERVISOR("Supervisor Técnico", "Marcelo Crisóstomo",
        canReview = true, canViewDashboard = true, canExport = true),
    ANALYST("Analista de Datos", "Valentina Rojas", canViewDashboard = true, canExport = true);

    /** Nombre corto para barras superiores y chips. */
    val shortLabel: String
        get() = when (this) { OPERATOR -> "Operador"; SUPERVISOR -> "Supervisor"; ANALYST -> "Analista" }

    companion object {
        /** Valor exacto o, si no coincide (o es nulo), el rol de menor privilegio. */
        fun fromName(value: String?): UserRole = entries.firstOrNull { it.name == value } ?: OPERATOR
    }
}
