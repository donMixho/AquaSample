package com.aldemar.aquasample.ui.viewmodel
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus

/** Filtros combinables del historial; null / vacío = sin restricción. Fechas en ms, inclusivas. */
data class HistoryFilter(
    val query: String = "",
    val status: SampleStatus? = null,
    val center: String? = null,
    val train: String? = null,
    val line: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
) {
    val isActive get() = query.isNotBlank() || status != null || center != null || train != null ||
        line != null || startDate != null || endDate != null
}

object SampleFilter {
    fun apply(samples: List<SampleEntity>, query: String, status: SampleStatus?, center: String?): List<SampleEntity> {
        val words = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return samples.filter { sample ->
            val searchable = listOf(sample.code, sample.center, sample.train,
                sample.line, sample.operatorName).joinToString(" ")
            words.all { searchable.contains(it, ignoreCase = true) } &&
                (status == null || sample.reviewStatus == status) &&
                (center == null || sample.center.equals(center, ignoreCase = true))
        }
    }

    fun apply(samples: List<SampleEntity>, f: HistoryFilter): List<SampleEntity> =
        HistoryAnalytics.inRange(apply(samples, f.query, f.status, f.center), f.startDate, f.endDate)
            .filter { (f.train == null || it.train.equals(f.train, true)) && (f.line == null || it.line.equals(f.line, true)) }
}
