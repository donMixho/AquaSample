package com.aldemar.aquasample.data.model

/**
 * Estados del ciclo de vida de una muestra en terreno.
 *
 * Flujo de estados:
 * 1. PENDIENTE: Muestra recién registrada por el operador en terreno, esperando revisión técnica.
 * 2. VALIDADO: Aprobada por el supervisor técnico conforme a la pauta de cultivo.
 * 3. OBSERVADO: El supervisor detectó una inconsistencia o requiere seguimiento/desdoble de cuerda.
 */
enum class SampleStatus(val label: String) {
    PENDIENTE("Pendiente"),
    VALIDADO("Validado"),
    OBSERVADO("Observado");

    companion object {
        /**
         * Permite convertir un String almacenado en Room de vuelta al Enum tipado.
         */
        fun fromString(value: String): SampleStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDIENTE
        }
    }
}
