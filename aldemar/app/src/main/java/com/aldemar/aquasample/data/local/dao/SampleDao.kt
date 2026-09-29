package com.aldemar.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operaciones sobre SQLite mediante Room.
 *
 * Principios aplicados:
 * - Operaciones asíncronas con 'suspend' para no bloquear el hilo principal (UI Thread).
 * - Reactividad con 'Flow<List<T>>': La UI de Jetpack Compose se actualiza automáticamente
 *   cada vez que se inserta o modifica un registro en la base de datos sin recargar manualmente.
 */
@Dao
interface SampleDao {

    /**
     * Obtiene el listado completo de muestras ordenadas cronológicamente (las más recientes primero).
     * Retorna Flow para que la interfaz reaccione en tiempo real.
     */
    @Query("SELECT * FROM samples ORDER BY createdAt DESC")
    fun getAllSamples(): Flow<List<SampleEntity>>

    /**
     * Consulta reactiva de una muestra específica por su ID primario.
     */
    @Query("SELECT * FROM samples WHERE id = :id LIMIT 1")
    fun getSampleById(id: Long): Flow<SampleEntity?>

    /**
     * Consulta directa de un solo tiro (one-shot) para comprobaciones puntuales.
     */
    @Query("SELECT * FROM samples WHERE id = :id LIMIT 1")
    suspend fun getSampleByIdOnce(id: Long): SampleEntity?

    /**
     * Inserta una nueva muestra tomada en terreno y retorna el ID autogenerado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: SampleEntity): Long

    /**
     * Inserta un conjunto de muestras (usado para precargar la base de datos de prueba).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SampleEntity>)

    /**
     * Actualiza todos los campos de una muestra existente.
     */
    @Update
    suspend fun updateSample(sample: SampleEntity)

    /**
     * Actualiza puntualmente el estado de revisión y dictamen técnico emitido por el supervisor.
     */
    @Query("UPDATE samples SET status = :status, supervisorComment = :comment, supervisorName = :supervisorName WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, comment: String?, supervisorName: String?)

    /**
     * Permite comprobar si la tabla ya tiene datos o si es la primera apertura de la app.
     */
    @Query("SELECT COUNT(*) FROM samples")
    suspend fun getSampleCount(): Int

    // ==========================================
    // Consultas para Catálogo de Centros
    // ==========================================

    @Query("SELECT * FROM centers ORDER BY name ASC")
    fun getAllCenters(): Flow<List<CenterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCenters(centers: List<CenterEntity>)

    @Query("SELECT COUNT(*) FROM centers")
    suspend fun getCenterCount(): Int
}
