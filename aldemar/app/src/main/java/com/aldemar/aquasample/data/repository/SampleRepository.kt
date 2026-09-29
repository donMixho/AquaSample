package com.aldemar.aquasample.data.repository

import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Patrón Repositorio (Repository Pattern).
 *
 * Propósito en la arquitectura MVVM:
 * - Es la única fuente de verdad (Single Source of Truth) para la aplicación.
 * - Desacopla los ViewModels de los detalles de implementación de la base de datos (Room).
 * - Ejecuta las operaciones de escritura en hilos de entrada/salida (Dispatchers.IO) para
 *   evitar que la interfaz sufra tirones o se congele durante el guardado.
 */
class SampleRepository(private val sampleDao: SampleDao) {

    // Flujo continuo de todas las muestras para el listado reactivo
    val allSamples: Flow<List<SampleEntity>> = sampleDao.getAllSamples()

    // Catálogo de centros disponibles
    val allCenters: Flow<List<CenterEntity>> = sampleDao.getAllCenters()

    /**
     * Obtiene una muestra por ID de forma reactiva (emite cada vez que se modifique).
     */
    fun getSampleById(id: Long): Flow<SampleEntity?> {
        return sampleDao.getSampleById(id)
    }

    /**
     * Obtiene la muestra una sola vez sin suscripción activa.
     */
    suspend fun getSampleByIdOnce(id: Long): SampleEntity? {
        return withContext(Dispatchers.IO) {
            sampleDao.getSampleByIdOnce(id)
        }
    }

    /**
     * Inserta una nueva muestra en la base de datos Room.
     */
    suspend fun insertSample(sample: SampleEntity): Long {
        return withContext(Dispatchers.IO) {
            sampleDao.insertSample(sample)
        }
    }

    /**
     * Actualiza una muestra completa.
     */
    suspend fun updateSample(sample: SampleEntity) {
        withContext(Dispatchers.IO) {
            sampleDao.updateSample(sample)
        }
    }

    /**
     * Actualiza el dictamen de supervisión (Aprobada o con Observaciones).
     */
    suspend fun updateStatus(id: Long, status: String, comment: String?, supervisorName: String?) {
        withContext(Dispatchers.IO) {
            sampleDao.updateStatus(id, status, comment, supervisorName)
        }
    }

    /**
     * Método de seguridad para asegurar que siempre haya centros y muestras cargadas,
     * incluso si la app se ejecuta por primera vez en un emulador recién creado.
     */
    suspend fun ensureInitialData() {
        withContext(Dispatchers.IO) {
            if (sampleDao.getCenterCount() == 0) {
                val centers = listOf(
                    CenterEntity(name = "Centro Huar", location = "Isla Huar - Calbuco"),
                    CenterEntity(name = "Centro Chidhuapi", location = "Canal Chidhuapi"),
                    CenterEntity(name = "Centro Calbuco", location = "Seno de Reloncaví"),
                    CenterEntity(name = "Centro Quellón", location = "Golfo Corcovado - Chiloé")
                )
                sampleDao.insertCenters(centers)
            }
            if (sampleDao.getSampleCount() == 0) {
                val samples = listOf(
                    SampleEntity(
                        code = "MUE-2026-001",
                        centerName = "Centro Huar",
                        trainNumber = "Tren 01",
                        lineNumber = "Línea 14",
                        sampleDate = "15-09-2026 10:30",
                        sectionLengthMeters = 1.0,
                        operatorName = "Carlos Delgado",
                        musselCount = 82,
                        observations = "Calibre homogéneo, fijación fuerte y baja presencia de macroalgas.",
                        status = "VALIDADO",
                        supervisorComment = "Conteo coherente con curva de crecimiento de engorda esperada.",
                        supervisorName = "Marcelo Crisóstomo",
                        createdAt = System.currentTimeMillis() - 86400000 * 5
                    ),
                    SampleEntity(
                        code = "MUE-2026-002",
                        centerName = "Centro Chidhuapi",
                        trainNumber = "Tren 02",
                        lineNumber = "Línea 08",
                        sampleDate = "20-09-2026 14:15",
                        sectionLengthMeters = 1.0,
                        operatorName = "Carlos Delgado",
                        musselCount = 115,
                        observations = "Semilla densa en tercio superior. Depredación leve por caracoles observada.",
                        status = "OBSERVADO",
                        supervisorComment = "Requiere seguimiento en próximo viaje para evaluar desdoble de cuerda.",
                        supervisorName = "Marcelo Crisóstomo",
                        createdAt = System.currentTimeMillis() - 86400000 * 2
                    ),
                    SampleEntity(
                        code = "MUE-2026-003",
                        centerName = "Centro Calbuco",
                        trainNumber = "Tren 01",
                        lineNumber = "Línea 03",
                        sampleDate = "28-09-2026 09:00",
                        sectionLengthMeters = 1.0,
                        operatorName = "Carlos Delgado",
                        musselCount = 68,
                        observations = "Muestreo de rutina post-temporal. Cuerdas sin desprendimiento ni roturas.",
                        status = "PENDIENTE",
                        supervisorComment = null,
                        supervisorName = null,
                        createdAt = System.currentTimeMillis()
                    )
                )
                sampleDao.insertSamples(samples)
            }
        }
    }
}
