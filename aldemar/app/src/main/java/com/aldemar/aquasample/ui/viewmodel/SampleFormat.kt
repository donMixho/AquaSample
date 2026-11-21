package com.aldemar.aquasample.ui.viewmodel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Conversión y formato puros (testeables en JVM). */
object SampleFormat {
    const val MAX_LENGTH_CM = 100_000

    fun dateTime(timestamp: Long): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    /** Texto del formulario en centímetros enteros ("100") -> Int, o null si no es un entero entre 1 y [MAX_LENGTH_CM]. */
    fun parseCm(text: String): Int? =
        text.trim().takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toIntOrNull()
            ?.takeIf { it in 1..MAX_LENGTH_CM }

    fun lengthLabel(cm: Int): String = if (cm % 100 == 0) "${cm / 100} m ($cm cm)" else "$cm cm"
}
