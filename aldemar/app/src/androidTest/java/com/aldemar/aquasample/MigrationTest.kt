package com.aldemar.aquasample

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aldemar.aquasample.data.local.AquaSampleDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val dbName = "migration-test.db"

    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AquaSampleDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test fun migration1To2KeepsRowsAndConvertsUnitsAndStatuses() {
        helper.createDatabase(dbName, 1).apply {
            fun insert(code: String, status: String, meters: Double) = execSQL(
                "INSERT INTO samples (code, centerName, trainNumber, lineNumber, sampleDate, sectionLengthMeters, " +
                    "operatorName, musselCount, observations, photoPath, status, supervisorComment, supervisorName, createdAt) " +
                    "VALUES ('$code','Huar','T1','L2','22-10-2026',$meters,'Ana',20,'obs','/p.jpg','$status',NULL,NULL,1792000000000)"
            )
            insert("A", "PENDIENTE", 1.0)
            insert("B", "VALIDADO", 1.5)
            insert("C", "OBSERVADO", 0.004)
            close()
        }
        val db = helper.runMigrationsAndValidate(dbName, 2, true, AquaSampleDatabase.MIGRATION_1_2)
        db.query("SELECT code, center, sectionLengthCm, timestamp, manualCount, reviewStatus, syncStatus, " +
            "markerCoordinatesJson, concession, photoPath FROM samples ORDER BY code").use { c ->
            assertEquals(3, c.count)
            c.moveToNext(); assertEquals("A", c.getString(0)); assertEquals("Huar", c.getString(1))
            assertEquals(100, c.getInt(2)); assertEquals(1792000000000L, c.getLong(3)); assertEquals(20, c.getInt(4))
            assertEquals("PENDIENTE_REVISION", c.getString(5)); assertEquals("PENDIENTE_SINCRONIZACION", c.getString(6))
            assertEquals("[]", c.getString(7)); assertEquals(true, c.isNull(8)); assertEquals("/p.jpg", c.getString(9))
            c.moveToNext(); assertEquals(150, c.getInt(2)); assertEquals("VALIDADA", c.getString(5))
            c.moveToNext(); assertEquals(1, c.getInt(2)); assertEquals("OBSERVADA", c.getString(5))
        }
    }
}
