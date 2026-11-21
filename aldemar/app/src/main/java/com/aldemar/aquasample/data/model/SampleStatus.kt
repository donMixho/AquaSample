package com.aldemar.aquasample.data.model

/**
 * Estado de REVISIÓN de una muestra (independiente de [SyncStatus]).
 *
 * BORRADOR -> PENDIENTE_REVISION -> (OBSERVADA -> CORREGIDA -> PENDIENTE_REVISION)* -> VALIDADA
 */
enum class SampleStatus(val label: String) {
    BORRADOR("Borrador"),
    PENDIENTE_REVISION("Pendiente"),
    OBSERVADA("Observada"),
    CORREGIDA("Corregida"),
    VALIDADA("Validada");

    companion object {
        /** Acepta también los nombres anteriores (PENDIENTE, VALIDADO, OBSERVADO). */
        fun fromString(value: String): SampleStatus = when (value.uppercase()) {
            "PENDIENTE" -> PENDIENTE_REVISION
            "VALIDADO" -> VALIDADA
            "OBSERVADO" -> OBSERVADA
            else -> entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDIENTE_REVISION
        }
    }
}
