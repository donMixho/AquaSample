package com.aldemar.aquasample.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de Datos Room SQLite de la aplicación AquaSample (ALDEMAR SpA).
 *
 * Características clave:
 * - Implementa el patrón Singleton (@Volatile INSTANCE) para evitar múltiples instancias concurrentes
 *   que consumirían memoria y provocarían bloqueos en SQLite.
 * - Registra un 'Callback' de creación para precargar datos iniciales automáticamente la primera vez
 *   que se crea el archivo 'aquasample_aldemar.db' en el almacenamiento interno.
 */
@Database(
    entities = [SampleEntity::class, CenterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AquaSampleDatabase : RoomDatabase() {

    abstract fun sampleDao(): SampleDao

    companion object {
        @Volatile
        private var INSTANCE: AquaSampleDatabase? = null

        /**
         * Retorna la instancia única de la base de datos Room.
         */
        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AquaSampleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AquaSampleDatabase::class.java,
                    "aquasample_aldemar.db"
                )
                    .addCallback(AquaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback invocado al crear la base de datos por primera vez.
     * Inserta los centros de la zona de Los Lagos y muestras de ejemplo con los 3 estados.
     */
    private class AquaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateInitialData(database.sampleDao())
                }
            }
        }

        suspend fun populateInitialData(dao: SampleDao) {
            // Precarga de centros de cultivo de la región
            val centers = listOf(
                CenterEntity(name = "Centro Huar", location = "Isla Huar - Calbuco"),
                CenterEntity(name = "Centro Chidhuapi", location = "Canal Chidhuapi"),
                CenterEntity(name = "Centro Calbuco", location = "Seno de Reloncaví"),
                CenterEntity(name = "Centro Quellón", location = "Golfo Corcovado - Chiloé")
            )
            dao.insertCenters(centers)

            // Muestras de prueba con trazabilidad y estados
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
            dao.insertSamples(samples)
        }
    }
}
