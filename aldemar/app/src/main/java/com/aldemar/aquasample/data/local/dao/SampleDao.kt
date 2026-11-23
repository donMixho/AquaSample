package com.aldemar.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO de muestras y centros. Operaciones 'suspend' y 'Flow' para no bloquear la UI
 * y que Compose reaccione a los cambios en la base.
 */
@Dao
interface SampleDao {
    @Transaction
    suspend fun finishUpload(snapshot: SampleEntity, status: SyncStatus) {
        if (getSampleByIdOnce(snapshot.id) == snapshot) updateSyncStatus(snapshot.id, status)
    }
    /** Merge atomically: never replace a locally edited row or its private photo URI. */
    @Transaction
    suspend fun mergeRemote(sample: SampleEntity): Boolean {
        val id = findIdByCode(sample.code)
        val local = id?.let { getSampleByIdOnce(it) }
        if (local != null && local.syncStatus != SyncStatus.SINCRONIZADO) return false
        if (local == null) insertSample(sample.copy(id = 0, photoPath = null, syncStatus = SyncStatus.SINCRONIZADO))
        else updateSample(sample.copy(id = local.id, photoPath = local.photoPath, syncStatus = SyncStatus.SINCRONIZADO))
        return true
    }
    @Query("SELECT id FROM samples WHERE code = :code LIMIT 1")
    suspend fun findIdByCode(code: String): Long?

    /** A retry of the same draft must never create a second sample. */
    @Transaction
    suspend fun insertSampleOnce(sample: SampleEntity): Long =
        findIdByCode(sample.code) ?: insertSample(sample)

    @Transaction
    suspend fun seedIfEmpty(centers: List<CenterEntity>, samples: List<SampleEntity>) {
        if (getCenterCount() == 0) insertCenters(centers)
        if (getSampleCount() == 0) insertSamples(samples)
    }

    /** Todas las muestras, las más recientes primero. */
    @Query("SELECT * FROM samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples WHERE id = :id LIMIT 1")
    fun getSampleById(id: Long): Flow<SampleEntity?>

    @Query("SELECT * FROM samples WHERE id = :id LIMIT 1")
    suspend fun getSampleByIdOnce(id: Long): SampleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: SampleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SampleEntity>)

    @Update
    suspend fun updateSample(sample: SampleEntity)

    /**
     * Dictamen del supervisor. El registro cambió localmente, por eso también fija
     * [syncStatus] (el repositorio pasa PENDIENTE_SINCRONIZACION).
     */
    @Query("UPDATE samples SET reviewStatus = :status, supervisorComment = :comment, supervisorName = :supervisorName, syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateReview(id: Long, status: SampleStatus, comment: String?, supervisorName: String?, syncStatus: SyncStatus)

    /**
     * Filtros combinables (null = sin restricción). Fechas inclusivas, en ms.
     * Centro, tren y línea se comparan sin distinguir mayúsculas.
     */
    @Query("""
        SELECT * FROM samples
        WHERE (:center IS NULL OR center = :center COLLATE NOCASE)
          AND (:train IS NULL OR train = :train COLLATE NOCASE)
          AND (:line IS NULL OR line = :line COLLATE NOCASE)
          AND (:status IS NULL OR reviewStatus = :status)
          AND (:startDate IS NULL OR timestamp >= :startDate)
          AND (:endDate IS NULL OR timestamp <= :endDate)
        ORDER BY timestamp DESC
    """)
    fun filterSamples(
        center: String?, train: String?, line: String?, status: SampleStatus?,
        startDate: Long?, endDate: Long?
    ): Flow<List<SampleEntity>>

    // ---- Sincronización offline ----
    @Query("SELECT * FROM samples WHERE syncStatus = :status ORDER BY timestamp ASC")
    fun getBySyncStatus(status: SyncStatus): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples WHERE syncStatus = :status ORDER BY timestamp ASC")
    suspend fun getBySyncStatusOnce(status: SyncStatus): List<SampleEntity>

    @Query("SELECT COUNT(*) FROM samples WHERE syncStatus = :status")
    fun countBySyncStatus(status: SyncStatus): Flow<Int>

    @Query("UPDATE samples SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus)

    @Query("SELECT COUNT(*) FROM samples")
    suspend fun getSampleCount(): Int

    // ---- Catálogo de centros ----
    @Query("SELECT * FROM centers ORDER BY name ASC")
    fun getAllCenters(): Flow<List<CenterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCenters(centers: List<CenterEntity>)

    @Query("SELECT COUNT(*) FROM centers")
    suspend fun getCenterCount(): Int
}
