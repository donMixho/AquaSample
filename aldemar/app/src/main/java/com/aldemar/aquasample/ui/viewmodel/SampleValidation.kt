package com.aldemar.aquasample.ui.viewmodel

import com.aldemar.aquasample.data.model.SampleCount

/** Pure rules: both the UI and the final save use the same validation. */
object SampleValidation {
    const val MAX_COUNT = SampleCount.MAX_COUNT

    fun validateLocation(center: String, train: String, line: String, operator: String,
                         lengthText: String, concession: String = ""): Map<String, String> = buildMap {
        for ((key, value) in listOf("center" to center, "train" to train, "line" to line, "operator" to operator)) {
            if (value.isBlank()) put(key, "Este campo es obligatorio.")
            else if (value.trim().length > 100) put(key, "Máximo 100 caracteres.")
        }
        // La concesión es opcional: solo se valida el largo.
        if (concession.trim().length > 100) put("concession", "Máximo 100 caracteres.")
        validateLength(lengthText)?.let { put("length", it) }
    }

    /** Longitud del tramo: entero en centímetros entre 1 y [SampleFormat.MAX_LENGTH_CM]. */
    fun validateLength(text: String): String? {
        val value = text.trim()
        return when {
            value.isEmpty() -> "Ingresa la longitud del tramo en centímetros."
            !value.all(Char::isDigit) -> "Solo números enteros en cm, sin decimales ni unidades (ej: 100)."
            SampleFormat.parseCm(value) != null -> null
            value.trimStart('0').isEmpty() -> "La longitud debe ser mayor que cero."
            else -> "La longitud máxima es ${SampleFormat.MAX_LENGTH_CM} cm."
        }
    }

    fun validateCount(text: String): String? {
        val count = text.trim().toIntOrNull()
        return if (count == null || count !in 0..MAX_COUNT)
            "Ingresa un entero entre 0 y 1.000.000; cero es un conteo válido." else null
    }
    fun validateReview(observed: Boolean, comment: String): String? = when {
        observed && comment.isBlank() -> "Explica el motivo de la observación."
        comment.length > 2000 -> "Máximo 2.000 caracteres."
        else -> null
    }
}
