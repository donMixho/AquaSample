package com.aldemar.aquasample.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity

@Database(entities = [SampleEntity::class, CenterEntity::class], version = 2, exportSchema = true)
abstract class AquaSampleDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    companion object {
        /**
         * v1 -> v2: renombra columnas, tramo m -> cm, createdAt -> timestamp, separa estado de
         * revisión (PENDIENTE/VALIDADO/OBSERVADO -> *_REVISION/VALIDADA/OBSERVADA) del de sincronización
         * y agrega concesión y marcas. No se pierden filas; todo lo existente queda pendiente de sincronizar.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `samples_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `code` TEXT NOT NULL, `concession` TEXT, " +
                        "`center` TEXT NOT NULL, `train` TEXT NOT NULL, `line` TEXT NOT NULL, " +
                        "`sectionLengthCm` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `operatorName` TEXT NOT NULL, " +
                        "`manualCount` INTEGER NOT NULL, `markerCoordinatesJson` TEXT NOT NULL, `observations` TEXT NOT NULL, " +
                        "`photoPath` TEXT, `reviewStatus` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, " +
                        "`supervisorComment` TEXT, `supervisorName` TEXT)"
                )
                // OR IGNORE: ante un código duplicado heredado, conserva la primera fila en vez de abortar la migración.
                db.execSQL(
                    "INSERT OR IGNORE INTO `samples_new` (`id`, `code`, `concession`, `center`, `train`, `line`, " +
                        "`sectionLengthCm`, `timestamp`, `operatorName`, `manualCount`, `markerCoordinatesJson`, " +
                        "`observations`, `photoPath`, `reviewStatus`, `syncStatus`, `supervisorComment`, `supervisorName`) " +
                        "SELECT `id`, `code`, NULL, `centerName`, `trainNumber`, `lineNumber`, " +
                        "MAX(1, CAST(ROUND(`sectionLengthMeters` * 100) AS INTEGER)), `createdAt`, `operatorName`, " +
                        "`musselCount`, '[]', `observations`, `photoPath`, " +
                        "CASE `status` WHEN 'VALIDADO' THEN 'VALIDADA' WHEN 'OBSERVADO' THEN 'OBSERVADA' " +
                        "ELSE 'PENDIENTE_REVISION' END, 'PENDIENTE_SINCRONIZACION', `supervisorComment`, `supervisorName` " +
                        "FROM `samples`"
                )
                db.execSQL("DROP TABLE `samples`")
                db.execSQL("ALTER TABLE `samples_new` RENAME TO `samples`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_samples_code` ON `samples` (`code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_samples_timestamp` ON `samples` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_samples_syncStatus` ON `samples` (`syncStatus`)")
            }
        }

        /** Único punto de construcción (base real y base demo) para que ambas migren igual. */
        fun build(context: Context, name: String): AquaSampleDatabase =
            Room.databaseBuilder(context.applicationContext, AquaSampleDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2)
                .build()

        @Volatile private var instance: AquaSampleDatabase? = null
        fun getDatabase(context: Context): AquaSampleDatabase = instance ?: synchronized(this) {
            instance ?: build(context, "aquasample_aldemar.db").also { instance = it }
        }
    }
}
