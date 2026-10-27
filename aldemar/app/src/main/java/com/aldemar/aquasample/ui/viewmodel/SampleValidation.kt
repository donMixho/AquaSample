package com.aldemar.aquasample.ui.viewmodel

/** Pure rules: both the UI and the final save use the same validation. */
object SampleValidation {
    const val MAX_COUNT = 1_000_000
    fun validateLocation(center: String, train: String, line: String, operator: String,
                         lengthText: String): Map<String, String> = buildMap {
        for ((key, value) in listOf("center" to center, "train" to train, "line" to line, "operator" to operator)) {
            if (value.isBlank()) put(key, "Este campo es obligatorio.")
            else if (value.trim().length > 100) put(key, "Máximo 100 caracteres.")
        }
        val length = lengthText.trim().replace(',', '.').toDoubleOrNull()
        if (length == null || !length.isFinite() || length <= 0.0)
            put("length", "La longitud debe ser un número mayor que cero.")
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
