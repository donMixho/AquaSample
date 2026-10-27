package com.aldemar.aquasample.ui.viewmodel
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus

object SampleFilter {
    fun apply(samples: List<SampleEntity>, query: String, status: SampleStatus?, center: String?): List<SampleEntity> {
        val words = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return samples.filter { sample ->
            val searchable = listOf(sample.code, sample.centerName, sample.trainNumber,
                sample.lineNumber, sample.operatorName).joinToString(" ")
            words.all { searchable.contains(it, ignoreCase = true) } &&
                (status == null || sample.status.equals(status.name, ignoreCase = true)) &&
                (center == null || sample.centerName.equals(center, ignoreCase = true))
        }
    }
}
