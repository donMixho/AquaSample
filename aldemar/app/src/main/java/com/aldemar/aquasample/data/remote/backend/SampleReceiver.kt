package com.aldemar.aquasample.data.remote.backend

import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.SyncStatus

/** Receives server records; the store performs the conflict check inside a transaction. */
class SampleReceiver(
    private val api: AquaSampleBackendApi,
    private val merge: suspend (SampleEntity) -> Boolean
) {
    suspend fun receive(): Int {
        // Validate the complete response before writing anything.
        val records = api.list().map { dto ->
            require(dto.code.isNotBlank() && dto.sectionLengthCm > 0 && dto.manualCount >= 0)
            SampleEntity(code = dto.code, concession = dto.concession, center = dto.center,
                train = dto.train, line = dto.line, sectionLengthCm = dto.sectionLengthCm,
                timestamp = dto.timestamp, operatorName = dto.operatorName, manualCount = dto.manualCount,
                markerCoordinatesJson = dto.markerCoordinatesJson, observations = dto.observations,
                reviewStatus = SampleStatus.valueOf(dto.reviewStatus), syncStatus = SyncStatus.SINCRONIZADO,
                supervisorComment = dto.supervisorComment, supervisorName = dto.supervisorName)
        }
        var received = 0
        for (record in records) if (merge(record)) received++
        return received
    }
}
