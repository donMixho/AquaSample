package com.aldemar.aquasample.ui.viewmodel

import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus

/** Métricas puras del dashboard. */
object DashboardMetrics {
    data class Kpis(val totalSamples: Int, val totalIndividuals: Int, val avgDensity: Double?,
                    val pending: Int, val validated: Int, val observed: Int)
    data class CenterBar(val center: String, val samples: Int, val individuals: Int)
    data class Point(val timestamp: Long, val count: Int, val density: Double)

    fun kpis(samples: List<SampleEntity>): Kpis {
        val densities = samples.filter { it.sectionLengthCm > 0 }.map { it.manualCount.toDouble() / it.sectionLengthCm }
        return Kpis(
            totalSamples = samples.size,
            totalIndividuals = samples.sumOf { it.manualCount },
            avgDensity = densities.takeIf { it.isNotEmpty() }?.average(),
            pending = samples.count { it.reviewStatus == SampleStatus.PENDIENTE_REVISION || it.reviewStatus == SampleStatus.CORREGIDA },
            validated = samples.count { it.reviewStatus == SampleStatus.VALIDADA },
            observed = samples.count { it.reviewStatus == SampleStatus.OBSERVADA }
        )
    }

    /** Muestras e individuos por centro, de mayor a menor número de individuos. */
    fun byCenter(samples: List<SampleEntity>): List<CenterBar> =
        samples.groupBy { it.center.trim().lowercase() }.map { (_, g) ->
            CenterBar(g.first().center.trim(), g.size, g.sumOf { it.manualCount })
        }.sortedWith(compareByDescending<CenterBar> { it.individuals }.thenBy { it.center.lowercase() })

    /** Conteo histórico (cronológico) de una línea. */
    fun evolution(samples: List<SampleEntity>, line: HistoryAnalytics.LineKey): List<Point> =
        HistoryAnalytics.forLine(samples, line).sortedBy { it.timestamp }.map {
            Point(it.timestamp, it.manualCount, if (it.sectionLengthCm > 0) it.manualCount.toDouble() / it.sectionLengthCm else 0.0)
        }
}
