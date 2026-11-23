package com.aldemar.aquasample.data.repository

import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.remote.backend.SyncStore
import com.aldemar.aquasample.data.model.SampleQuery
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositorio: única fuente de verdad para los ViewModels. Desacopla Room y ejecuta
 * las escrituras en Dispatchers.IO.
 */
class SampleRepository(
    private val sampleDao: SampleDao,
    /** Se invoca tras cada cambio local que debe sincronizarse (dispara WorkManager). */
    private val onLocalChange: () -> Unit = {}
) : SyncStore {
    override suspend fun finishUpload(snapshot: SampleEntity, status: SyncStatus) = withContext(Dispatchers.IO) {
        sampleDao.finishUpload(snapshot, status)
    }
    suspend fun receiveRemote(sample: SampleEntity): Boolean = withContext(Dispatchers.IO) {
        sampleDao.mergeRemote(sample)
    }

    suspend fun findIdByCode(code: String): Long? = sampleDao.findIdByCode(code)

    val allSamples: Flow<List<SampleEntity>> = sampleDao.getAllSamples()
    val allCenters: Flow<List<CenterEntity>> = sampleDao.getAllCenters()

    fun getSampleById(id: Long): Flow<SampleEntity?> = sampleDao.getSampleById(id)

    suspend fun getSampleByIdOnce(id: Long): SampleEntity? =
        withContext(Dispatchers.IO) { sampleDao.getSampleByIdOnce(id) }

    suspend fun insertSample(sample: SampleEntity): Long =
        withContext(Dispatchers.IO) { sampleDao.insertSampleOnce(sample) }.also { onLocalChange() }

    suspend fun updateSample(sample: SampleEntity) {
        withContext(Dispatchers.IO) {
            sampleDao.updateSample(sample.copy(syncStatus = SyncStatus.PENDIENTE_SINCRONIZACION))
        }
        onLocalChange()
    }

    /** Dictamen de supervisión. La muestra queda pendiente de sincronizar. */
    suspend fun updateStatus(id: Long, status: SampleStatus, comment: String?, supervisorName: String?) {
        withContext(Dispatchers.IO) {
            sampleDao.updateReview(id, status, comment, supervisorName, SyncStatus.PENDIENTE_SINCRONIZACION)
        }
        onLocalChange()
    }

    /** Historial con filtros combinables: centro, tren, línea, estado y rango de fechas. */
    fun filterSamples(query: SampleQuery): Flow<List<SampleEntity>> = sampleDao.filterSamples(
        query.center, query.train, query.line, query.reviewStatus, query.startDate, query.endDate
    )

    fun filterSamples(
        center: String? = null, train: String? = null, line: String? = null,
        reviewStatus: SampleStatus? = null, startDate: Long? = null, endDate: Long? = null
    ): Flow<List<SampleEntity>> = filterSamples(SampleQuery(center, train, line, reviewStatus, startDate, endDate))

    // ---- Sincronización (lo consumirá WorkManager en un paso posterior) ----
    val pendingSync: Flow<List<SampleEntity>> = sampleDao.getBySyncStatus(SyncStatus.PENDIENTE_SINCRONIZACION)
    val pendingSyncCount: Flow<Int> = sampleDao.countBySyncStatus(SyncStatus.PENDIENTE_SINCRONIZACION)

    suspend fun getPendingSyncOnce(): List<SampleEntity> =
        withContext(Dispatchers.IO) { sampleDao.getBySyncStatusOnce(SyncStatus.PENDIENTE_SINCRONIZACION) }

    override suspend fun getUnsynced(): List<SampleEntity> = withContext(Dispatchers.IO) {
        sampleDao.getBySyncStatusOnce(SyncStatus.PENDIENTE_SINCRONIZACION) +
            sampleDao.getBySyncStatusOnce(SyncStatus.ERROR_SINCRONIZACION)
    }

    override suspend fun markSyncStatus(id: Long, status: SyncStatus) {
        withContext(Dispatchers.IO) { sampleDao.updateSyncStatus(id, status) }
    }

    /** Garantiza centros (y opcionalmente muestras demo) en una base recién creada. */
    suspend fun ensureInitialData(includeDemoSamples: Boolean = true) {
        withContext(Dispatchers.IO) {
            val centers = listOf(
                CenterEntity(name = "Centro Huar", location = "Isla Huar - Calbuco"),
                CenterEntity(name = "Centro Chidhuapi", location = "Canal Chidhuapi"),
                CenterEntity(name = "Centro Calbuco", location = "Seno de Reloncaví"),
                CenterEntity(name = "Centro Quellón", location = "Golfo Corcovado - Chiloé")
            )
            val now = System.currentTimeMillis()
            val day = 86_400_000L
            val samples = listOf(
                SampleEntity(
                    code = "MUE-2026-001", concession = "Concesión Calbuco Norte",
                    center = "Centro Huar", train = "Tren 01", line = "Línea 14",
                    sectionLengthCm = 100, timestamp = now - day * 5,
                    operatorName = "Carlos Delgado", manualCount = 82,
                    observations = "Calibre homogéneo, fijación fuerte y baja presencia de macroalgas.",
                    reviewStatus = SampleStatus.VALIDADA, syncStatus = SyncStatus.SINCRONIZADO,
                    supervisorComment = "Conteo coherente con curva de crecimiento de engorda esperada.",
                    supervisorName = "Marcelo Crisóstomo"
                ),
                SampleEntity(
                    code = "MUE-2026-002", concession = "Concesión Chidhuapi",
                    center = "Centro Chidhuapi", train = "Tren 02", line = "Línea 08",
                    sectionLengthCm = 100, timestamp = now - day * 2,
                    operatorName = "Carlos Delgado", manualCount = 115,
                    observations = "Semilla densa en tercio superior. Depredación leve por caracoles observada.",
                    reviewStatus = SampleStatus.OBSERVADA, syncStatus = SyncStatus.PENDIENTE_SINCRONIZACION,
                    supervisorComment = "Requiere seguimiento en próximo viaje para evaluar desdoble de cuerda.",
                    supervisorName = "Marcelo Crisóstomo"
                ),
                SampleEntity(
                    code = "MUE-2026-003", concession = "Concesión Calbuco Norte",
                    center = "Centro Calbuco", train = "Tren 01", line = "Línea 03",
                    sectionLengthCm = 100, timestamp = now,
                    operatorName = "Carlos Delgado", manualCount = 68,
                    observations = "Muestreo de rutina post-temporal. Cuerdas sin desprendimiento ni roturas.",
                    reviewStatus = SampleStatus.PENDIENTE_REVISION, syncStatus = SyncStatus.PENDIENTE_SINCRONIZACION
                )
            )
            sampleDao.seedIfEmpty(centers, if (includeDemoSamples) samples else emptyList())
        }
    }
}
