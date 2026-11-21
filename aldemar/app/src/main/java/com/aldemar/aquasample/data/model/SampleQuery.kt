package com.aldemar.aquasample.data.model

/** Filtros combinables del historial; null = sin restricción. Fechas en ms, ambos extremos inclusivos. */
data class SampleQuery(
    val center: String? = null,
    val train: String? = null,
    val line: String? = null,
    val reviewStatus: SampleStatus? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)
