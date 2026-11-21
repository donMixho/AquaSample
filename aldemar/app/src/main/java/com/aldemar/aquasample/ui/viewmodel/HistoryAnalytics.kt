package com.aldemar.aquasample.ui.viewmodel

import com.aldemar.aquasample.data.local.entity.SampleEntity
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

/** Cálculos puros de historial: filtrado por rango, líneas y comparación de períodos. */
object HistoryAnalytics {
    data class LineKey(val center: String, val train: String, val line: String) {
        val label: String get() = "$center · $train · $line"
        fun matches(s: SampleEntity) = s.center.trim().equals(center, true) &&
            s.train.trim().equals(train, true) && s.line.trim().equals(line, true)
    }

    data class Stats(val samples: Int, val individuals: Int, val lengthCm: Long) {
        /** Individuos por cm de cuerda muestreada (null si no hay tramo). */
        val density: Double? get() = if (lengthCm > 0) individuals.toDouble() / lengthCm else null
    }

    data class Variation(val absolute: Double, val percent: Double?)
    data class Comparison(val base: Stats, val current: Stats, val individuals: Variation, val density: Variation?)

    fun keyOf(s: SampleEntity) = LineKey(s.center.trim(), s.train.trim(), s.line.trim())

    fun lines(samples: List<SampleEntity>): List<LineKey> =
        samples.map(::keyOf).distinctBy { it.label.lowercase() }.sortedBy { it.label.lowercase() }

    fun forLine(samples: List<SampleEntity>, key: LineKey) = samples.filter(key::matches)

    /** Rango inclusivo en ms; null = sin límite. */
    fun inRange(samples: List<SampleEntity>, start: Long?, end: Long?) =
        samples.filter { (start == null || it.timestamp >= start) && (end == null || it.timestamp <= end) }

    fun stats(samples: List<SampleEntity>) =
        Stats(samples.size, samples.sumOf { it.manualCount }, samples.sumOf { it.sectionLengthCm.toLong() })

    fun variation(base: Double, current: Double) =
        Variation(current - base, if (base != 0.0) (current - base) / abs(base) * 100.0 else null)

    /** Variación del período [current] respecto de [base]. */
    fun compare(base: List<SampleEntity>, current: List<SampleEntity>): Comparison {
        val a = stats(base)
        val b = stats(current)
        val da = a.density
        val db = b.density
        return Comparison(a, b, variation(a.individuals.toDouble(), b.individuals.toDouble()),
            if (da != null && db != null) variation(da, db) else null)
    }

    /** El selector de fecha entrega medianoche UTC; se convierte al día local (inicio / fin). */
    fun localDayStart(utcMillis: Long): Long = localDay(utcMillis, false)
    fun localDayEnd(utcMillis: Long): Long = localDay(utcMillis, true)
    private fun localDay(utcMillis: Long, end: Boolean): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
            if (end) { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
        }.timeInMillis
    }
}
