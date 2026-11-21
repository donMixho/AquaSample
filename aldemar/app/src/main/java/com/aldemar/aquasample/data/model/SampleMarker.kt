package com.aldemar.aquasample.data.model

import java.io.Serializable

/**
 * Marca de conteo sobre la fotografía.
 *
 * [x] e [y] están normalizadas entre 0f y 1f respecto del tamaño de la imagen ORIGINAL
 * (0,0 = esquina superior izquierda; 1,1 = inferior derecha). Al no depender de píxeles de
 * pantalla, no se desfasan con el zoom, la rotación del teléfono ni el tamaño del visor.
 */
data class SampleMarker(val x: Float, val y: Float) : Serializable {
    companion object {
        /** Crea una marca solo si ambas coordenadas son finitas y están dentro de 0..1. */
        fun ofOrNull(x: Float, y: Float): SampleMarker? =
            if (x.isFinite() && y.isFinite() && x in 0f..1f && y in 0f..1f) SampleMarker(x, y) else null
    }
}

/**
 * Serialización pura (sin org.json, para poder probarla en JVM) de las marcas a
 * `markerCoordinatesJson`: `[{"x":0.25,"y":0.5},{"x":0.1,"y":0.9}]`.
 */
object MarkerSerializer {
    private const val NUM = """-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?"""
    private const val ENTRY = """\{\s*"x"\s*:\s*($NUM)\s*,\s*"y"\s*:\s*($NUM)\s*\}"""
    private val entry = Regex(ENTRY).toPattern()

    const val EMPTY = "[]"

    fun toJson(markers: List<SampleMarker>): String =
        markers.joinToString(separator = ",", prefix = "[", postfix = "]") {
            "{\"x\":${round(it.x)},\"y\":${round(it.y)}}"
        }

    /**
     * Lee el JSON o devuelve null si no tiene exactamente el formato esperado o si alguna
     * coordenada está fuera de 0..1 (no se "arregla" en silencio un dato corrupto).
     */
    fun fromJsonOrNull(json: String?): List<SampleMarker>? {
        if (json == null) return null
        val end = json.length
        val matcher = entry.matcher(json)
        var i = skipWhitespace(json, 0)
        if (i >= end || json[i] != '[') return null
        i = skipWhitespace(json, i + 1)
        val out = ArrayList<SampleMarker>()
        if (i < end && json[i] == ']') return if (skipWhitespace(json, i + 1) == end) out else null
        // Recorrido iterativo: una regex global con repetición recursiva desbordaría la pila con miles de marcas.
        while (true) {
            matcher.region(i, end)
            if (!matcher.lookingAt()) return null
            out += SampleMarker.ofOrNull(matcher.group(1)!!.toFloat(), matcher.group(2)!!.toFloat()) ?: return null
            i = skipWhitespace(json, matcher.end())
            if (i >= end) return null
            when (json[i]) {
                ',' -> i = skipWhitespace(json, i + 1)
                ']' -> return if (skipWhitespace(json, i + 1) == end) out else null
                else -> return null
            }
        }
    }

    private fun skipWhitespace(s: String, from: Int): Int {
        var i = from
        while (i < s.length && s[i].isWhitespace()) i++
        return i
    }

    /** Variante tolerante para la UI: un JSON inválido equivale a "sin marcas". */
    fun fromJson(json: String?): List<SampleMarker> = fromJsonOrNull(json).orEmpty()

    /** 5 decimales (~1 px en una imagen de 100.000 px) mantienen el JSON corto. */
    private fun round(v: Float): Float = Math.round(v * 100_000f) / 100_000f
}

/**
 * Regla única del conteo: `total = marcas + ajusteManual`.
 *
 * `manualCount` guarda siempre este total exacto, por lo que nunca diverge de las marcas
 * ni del ajuste. El total se mantiene entre 0 y [MAX_COUNT] normalizando el ajuste.
 */
object SampleCount {
    const val MAX_COUNT = 1_000_000
    const val MAX_MARKERS = 2_000

    fun total(markerCount: Int, adjustment: Int): Int = (markerCount + adjustment).coerceIn(0, MAX_COUNT)

    /** Ajuste que hace que, con [markerCount] marcas, el total sea exactamente [target]. */
    fun adjustmentFor(target: Int, markerCount: Int): Int = target.coerceIn(0, MAX_COUNT) - markerCount

    /** Acota el ajuste para que `marcas + ajuste` quede dentro de 0..MAX_COUNT sin recortes. */
    fun normalizeAdjustment(markerCount: Int, adjustment: Int): Int =
        adjustment.coerceIn(-markerCount, MAX_COUNT - markerCount)
}
