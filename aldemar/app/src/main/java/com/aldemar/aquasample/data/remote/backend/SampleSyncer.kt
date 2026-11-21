package com.aldemar.aquasample.data.remote.backend

import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SyncStatus
import kotlin.coroutines.cancellation.CancellationException

/** Acceso mínimo a la BD local que necesita la sincronización (lo implementa SampleRepository). */
interface SyncStore {
    /** Muestras PENDIENTE_SINCRONIZACION y ERROR_SINCRONIZACION (reintento). */
    suspend fun getUnsynced(): List<SampleEntity>
    suspend fun markSyncStatus(id: Long, status: SyncStatus)
}

data class SyncResult(val synced: Int, val failed: Int)

class SampleSyncer(private val store: SyncStore, private val api: AquaSampleBackendApi) {
    /**
     * Envía cada muestra con su `code` único: POST; si el servidor ya la tiene (409) hace PUT,
     * así un reintento nunca duplica. Ante cualquier fallo conserva el dato local como ERROR.
     */
    suspend fun sync(): SyncResult {
        var ok = 0
        var bad = 0
        for (sample in store.getUnsynced()) {
            val success = try {
                val dto = sample.toDto()
                val r = api.create(dto)
                r.isSuccessful || (r.code() == 409 && api.update(dto.code, dto).isSuccessful)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            store.markSyncStatus(sample.id, if (success) SyncStatus.SINCRONIZADO else SyncStatus.ERROR_SINCRONIZACION)
            if (success) ok++ else bad++
        }
        return SyncResult(ok, bad)
    }
}
