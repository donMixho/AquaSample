package com.aldemar.aquasample

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquasample.data.local.AquaSampleDatabase
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.SyncStatus
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun sample() = SampleEntity(code="TEST-${UUID.randomUUID()}", center="Huar", train="Tren 1",
        line="Línea 2", timestamp=System.currentTimeMillis(), sectionLengthCm=150,
        operatorName="Prueba", manualCount=12, observations="Prueba automatizada")
    @Test fun savedSampleAndReviewSurviveClosingAndReopeningDatabase() = runBlocking {
        val name = "persistence-${UUID.randomUUID()}.db"
        var db = Room.databaseBuilder(context,AquaSampleDatabase::class.java,name).build()
        try {
            val record = sample()
            val id = SampleRepository(db.sampleDao()).insertSample(record)
            db.sampleDao().updateReview(id,SampleStatus.OBSERVADA,"Repetir conteo","Supervisora",SyncStatus.PENDIENTE_SINCRONIZACION)
            db.close()
            db = Room.databaseBuilder(context,AquaSampleDatabase::class.java,name).build()
            val loaded = db.sampleDao().getSampleByIdOnce(id)!!
            assertEquals(record.code,loaded.code)
            assertEquals(12,loaded.manualCount)
            assertEquals(SampleStatus.OBSERVADA,loaded.reviewStatus)
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
    @Test fun filtersCombineCenterTrainLineStatusAndDateRange() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context,AquaSampleDatabase::class.java).build()
        try {
            val repo = SampleRepository(db.sampleDao())
            fun s(code: String, center: String, train: String, line: String, ts: Long, st: SampleStatus) =
                sample().copy(code = code, center = center, train = train, line = line, timestamp = ts, reviewStatus = st)
            repo.insertSample(s("A", "Huar", "T1", "L1", 1_000, SampleStatus.PENDIENTE_REVISION))
            repo.insertSample(s("B", "Huar", "T1", "L2", 2_000, SampleStatus.VALIDADA))
            repo.insertSample(s("C", "Calbuco", "T2", "L1", 3_000, SampleStatus.VALIDADA))
            fun codes(vararg a: Any?) = runBlocking {
                repo.filterSamples(a[0] as String?, a[1] as String?, a[2] as String?, a[3] as SampleStatus?,
                    a[4] as Long?, a[5] as Long?).first().map { it.code }
            }
            assertEquals(listOf("C","B","A"), codes(null,null,null,null,null,null))
            assertEquals(listOf("B","A"), codes("huar",null,null,null,null,null))
            assertEquals(listOf("B"), codes("Huar","T1","L2",SampleStatus.VALIDADA,null,null))
            assertEquals(listOf("C","B"), codes(null,null,null,SampleStatus.VALIDADA,2_000L,3_000L))
            assertEquals(listOf("B"), codes(null,null,null,null,1_500L,2_500L))
            assertEquals(listOf("C","A"), codes(null,null,"L1",null,null,null))
        } finally { db.close() }
    }
    @Test fun pendingSyncQueryReturnsOnlyPendingAndReviewMarksPending() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context,AquaSampleDatabase::class.java).build()
        try {
            val repo = SampleRepository(db.sampleDao())
            val synced = repo.insertSample(sample().copy(syncStatus = SyncStatus.SINCRONIZADO))
            val pending = repo.insertSample(sample())
            assertEquals(listOf(pending), repo.getPendingSyncOnce().map { it.id })
            assertEquals(1, repo.pendingSyncCount.first())
            repo.updateStatus(synced, SampleStatus.VALIDADA, "ok", "Sup")
            assertEquals(2, repo.getPendingSyncOnce().size)
            repo.markSyncStatus(synced, SyncStatus.ERROR_SINCRONIZACION)
            assertEquals(SyncStatus.ERROR_SINCRONIZACION, db.sampleDao().getSampleByIdOnce(synced)!!.syncStatus)
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
