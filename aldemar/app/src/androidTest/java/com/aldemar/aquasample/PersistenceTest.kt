package com.aldemar.aquasample

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquasample.data.local.AquaSampleDatabase
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun sample() = SampleEntity(code="TEST-${UUID.randomUUID()}", centerName="Huar", trainNumber="Tren 1",
        lineNumber="Línea 2", sampleDate="22-10-2026 10:00", sectionLengthMeters=1.5,
        operatorName="Prueba", musselCount=12, observations="Prueba automatizada")
    @Test fun savedSampleAndReviewSurviveClosingAndReopeningDatabase() = runBlocking {
        val name = "persistence-${UUID.randomUUID()}.db"
        var db = Room.databaseBuilder(context,AquaSampleDatabase::class.java,name).build()
        try {
            val record = sample()
            val id = SampleRepository(db.sampleDao()).insertSample(record)
            db.sampleDao().updateStatus(id,"OBSERVADO","Repetir conteo","Supervisora")
            db.close()
            db = Room.databaseBuilder(context,AquaSampleDatabase::class.java,name).build()
            val loaded = db.sampleDao().getSampleByIdOnce(id)!!
            assertEquals(record.code,loaded.code)
            assertEquals(12,loaded.musselCount)
            assertEquals("OBSERVADO",loaded.status)
            assertEquals("Repetir conteo",loaded.supervisorComment)
        } finally { db.close(); context.deleteDatabase(name) }
    }
    @Test fun concurrentRetriesOfSameDraftInsertExactlyOneRow() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context,AquaSampleDatabase::class.java).build()
        try {
            val repository = SampleRepository(db.sampleDao())
            val sample = sample()
            val ids = coroutineScope { (1..8).map { async(Dispatchers.IO) { repository.insertSample(sample) } }.awaitAll() }
            assertEquals(1,ids.distinct().size)
            assertEquals(1,db.sampleDao().getSampleCount())
        } finally { db.close() }
    }
    @Test fun initialDataIsNotDuplicatedWhenInitializationRepeats() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context,AquaSampleDatabase::class.java).build()
        try {
            val repository = SampleRepository(db.sampleDao())
            coroutineScope { (1..4).map { async(Dispatchers.IO) { repository.ensureInitialData() } }.awaitAll() }
            assertEquals(4,db.sampleDao().getCenterCount())
            assertEquals(3,db.sampleDao().getSampleCount())
        } finally { db.close() }
    }
}
