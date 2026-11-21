package com.aldemar.aquasample.remote

import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SyncStatus
import com.aldemar.aquasample.data.remote.backend.BackendClient
import com.aldemar.aquasample.data.remote.backend.SampleSyncer
import com.aldemar.aquasample.data.remote.backend.SyncStore
import com.aldemar.aquasample.data.remote.marine.MarineCache
import com.aldemar.aquasample.data.remote.marine.MarineConditions
import com.aldemar.aquasample.data.remote.marine.MarineLocation
import com.aldemar.aquasample.data.remote.marine.MarineRepository
import com.aldemar.aquasample.data.remote.marine.OpenMeteoMarineApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private class FakeStore(samples: List<SampleEntity>) : SyncStore {
    val status = samples.associate { it.id to it.syncStatus }.toMutableMap()
    private val all = samples
    override suspend fun getUnsynced() = all.filter { status[it.id] != SyncStatus.SINCRONIZADO }
    override suspend fun markSyncStatus(id: Long, status: SyncStatus) { this.status[id] = status }
}

private class FakeCache : MarineCache {
    var saved: MarineConditions? = null
    override suspend fun location() = MarineLocation.DEFAULT
    override suspend fun cached() = saved
    override suspend fun save(conditions: MarineConditions) { saved = conditions }
}

private fun sample(id: Long, code: String, sync: SyncStatus = SyncStatus.PENDIENTE_SINCRONIZACION) = SampleEntity(
    id = id, code = code, center = "Centro Huar", train = "T1", line = "L1", sectionLengthCm = 100,
    timestamp = 1L, operatorName = "Op", manualCount = 10, observations = "ok", syncStatus = sync
)

class RemoteTest {
    private lateinit var server: MockWebServer

    @Before fun setUp() { server = MockWebServer().also { it.start() } }
    @After fun tearDown() { server.shutdown() }

    private fun url() = server.url("/api/").toString()

    @Test fun syncMarksSynchronizedAndSendsUniqueCode() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("{}"))
        val store = FakeStore(listOf(sample(1, "MUE-1")))
        val r = SampleSyncer(store, BackendClient.create(url())).sync()
        assertEquals(1, r.synced)
        assertEquals(SyncStatus.SINCRONIZADO, store.status[1L])
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/samples", req.path)
        assertTrue(req.body.readUtf8().contains("\"code\":\"MUE-1\""))
    }

    @Test fun conflictFallsBackToPutWithoutDuplicating() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(409))
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        val store = FakeStore(listOf(sample(1, "MUE-1")))
        SampleSyncer(store, BackendClient.create(url())).sync()
        server.takeRequest()
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/api/samples/MUE-1", put.path)
        assertEquals(SyncStatus.SINCRONIZADO, store.status[1L])
    }

    @Test fun serverErrorKeepsDataAsErrorAndRetriesLater() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val store = FakeStore(listOf(sample(1, "MUE-1")))
        val r = SampleSyncer(store, BackendClient.create(url())).sync()
        assertEquals(1, r.failed)
        assertEquals(SyncStatus.ERROR_SINCRONIZACION, store.status[1L])
        // El error se reintenta en la siguiente corrida
        server.enqueue(MockResponse().setResponseCode(201).setBody("{}"))
        SampleSyncer(store, BackendClient.create(url())).sync()
        assertEquals(SyncStatus.SINCRONIZADO, store.status[1L])
    }

    @Test fun serverDownDoesNotThrow() = runBlocking {
        val u = url()
        server.shutdown()
        val store = FakeStore(listOf(sample(1, "A"), sample(2, "B")))
        val r = SampleSyncer(store, BackendClient.create(u)).sync()
        assertEquals(2, r.failed)
        assertEquals(SyncStatus.ERROR_SINCRONIZACION, store.status[2L])
    }

    private fun marineApi() = Retrofit.Builder().baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create()).build().create(OpenMeteoMarineApi::class.java)

    @Test fun marineParsesAndCaches() = runBlocking {
        server.enqueue(MockResponse().setBody(
            """{"current":{"time":"2026-01-01T10:00","wave_height":0.8,"wave_direction":200.0,"wave_period":6.5,"sea_surface_temperature":11.2}}"""
        ))
        val cache = FakeCache()
        val s = MarineRepository(marineApi(), cache) { 123L }.load()
        assertFalse(s.stale)
        assertEquals(0.8, s.conditions!!.waveHeightM!!, 0.0)
        assertEquals(11.2, cache.saved!!.seaTempC!!, 0.0)
        val req = server.takeRequest().path!!
        assertTrue(req.contains("latitude=-41.47") && req.contains("longitude=-72.94") && req.contains("wave_height"))
    }

    @Test fun marineFallsBackToCacheOffline() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val cache = FakeCache().apply { saved = MarineConditions("x", 0.0, 0.0, 1L, 1.0, null, 5.0, 10.0) }
        val s = MarineRepository(marineApi(), cache).load()
        assertTrue(s.stale)
        assertNotNull(s.error)
        assertEquals(1.0, s.conditions!!.waveHeightM!!, 0.0)
    }

    @Test fun marineWithoutCacheReturnsNullConditions() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        assertNull(MarineRepository(marineApi(), FakeCache()).load().conditions)
    }
}
