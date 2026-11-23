package com.aldemar.aquasample.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aldemar.aquasample.AquaSampleApp
import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleQuery
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.SyncStatus
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.remote.AppSettings
import com.aldemar.aquasample.data.remote.backend.AquaSampleBackendApi
import com.aldemar.aquasample.data.remote.backend.BackendClient
import com.aldemar.aquasample.data.remote.backend.ReviewDto
import com.aldemar.aquasample.data.remote.backend.SampleSyncer
import com.aldemar.aquasample.data.remote.backend.SyncStore
import com.aldemar.aquasample.data.remote.marine.MarineCache
import com.aldemar.aquasample.data.remote.marine.MarineCurrent
import com.aldemar.aquasample.data.remote.marine.MarineLocation
import com.aldemar.aquasample.data.remote.marine.MarineRepository
import com.aldemar.aquasample.data.remote.marine.MarineResponse
import com.aldemar.aquasample.data.remote.marine.OpenMeteoMarineApi
import com.aldemar.aquasample.data.repository.AuthAccount
import com.aldemar.aquasample.data.repository.FirebaseAuthRepository
import com.aldemar.aquasample.data.repository.PhotoStorage
import com.aldemar.aquasample.data.repository.SampleRepository
import com.aldemar.aquasample.worker.SyncSamplesWorker
import com.aldemar.aquasample.worker.SyncScheduler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

private fun sample(id: Long = 1, code: String = "MUE-$id") = SampleEntity(id = id, code = code, center = "C", train = "T",
    line = "L", sectionLengthCm = 100, timestamp = 1L, operatorName = "Op", manualCount = 3, observations = "ok")

class DataLayerMockkTest {
    @After fun tearDown() { unmockkAll() }

    // ---------- SampleRepository ----------
    private fun daoMock(): SampleDao {
        val dao = mockk<SampleDao>()
        every { dao.getAllSamples() } returns flowOf(listOf(sample()))
        every { dao.getAllCenters() } returns flowOf(listOf(CenterEntity(name = "C", location = "x")))
        every { dao.getBySyncStatus(any()) } returns flowOf(emptyList())
        every { dao.countBySyncStatus(any()) } returns flowOf(2)
        every { dao.getSampleById(any()) } returns flowOf(sample())
        every { dao.filterSamples(any(), any(), any(), any(), any(), any()) } returns flowOf(listOf(sample()))
        coEvery { dao.findIdByCode(any()) } returns 4L
        coEvery { dao.getSampleByIdOnce(any()) } returns sample()
        coEvery { dao.insertSampleOnce(any()) } returns 5L
        coEvery { dao.updateSample(any()) } returns Unit
        coEvery { dao.updateReview(any(), any(), any(), any(), any()) } returns Unit
        coEvery { dao.getBySyncStatusOnce(SyncStatus.PENDIENTE_SINCRONIZACION) } returns listOf(sample(1))
        coEvery { dao.getBySyncStatusOnce(SyncStatus.ERROR_SINCRONIZACION) } returns listOf(sample(2))
        coEvery { dao.updateSyncStatus(any(), any()) } returns Unit
        coEvery { dao.seedIfEmpty(any(), any()) } returns Unit
        return dao
    }

    @Test fun repositoryDelegatesToDaoAndNotifiesChanges() = runBlocking {
        val dao = daoMock(); var changes = 0
        val repo = SampleRepository(dao) { changes++ }
        assertEquals(4L, repo.findIdByCode("x"))
        assertEquals(1, repo.allSamples.first().size); assertEquals(1, repo.allCenters.first().size)
        assertEquals(1, repo.getSampleById(1).first()?.id?.toInt())
        assertEquals(1L, repo.getSampleByIdOnce(1)?.id)
        assertEquals(5L, repo.insertSample(sample()))
        repo.updateSample(sample())
        coVerify { dao.updateSample(match { it.syncStatus == SyncStatus.PENDIENTE_SINCRONIZACION }) }
        repo.updateStatus(1, SampleStatus.VALIDADA, "c", "s")
        assertEquals(3, changes)
        assertEquals(1, repo.filterSamples(SampleQuery(center = "C")).first().size)
        assertEquals(1, repo.filterSamples(center = "C").first().size)
        assertEquals(1, repo.filterSamples().first().size)
        assertTrue(repo.pendingSync.first().isEmpty()); assertEquals(2, repo.pendingSyncCount.first())
        assertEquals(1, repo.getPendingSyncOnce().size)
        assertEquals(listOf(1L, 2L), repo.getUnsynced().map { it.id })
        repo.markSyncStatus(1, SyncStatus.SINCRONIZADO)
        coVerify { dao.updateSyncStatus(1, SyncStatus.SINCRONIZADO) }
        repo.ensureInitialData(); repo.ensureInitialData(includeDemoSamples = false)
        coVerify { dao.seedIfEmpty(match { it.size == 4 }, match { it.size == 3 }) }
        coVerify { dao.seedIfEmpty(any(), match { it.isEmpty() }) }
        Unit
    }

    @Test fun repositoryWorksWithoutChangeCallback() = runBlocking {
        val repo = SampleRepository(daoMock())
        repo.insertSample(sample()); repo.updateStatus(1, SampleStatus.OBSERVADA, null, null)
    }

    // ---------- Simple models ----------
    @Test fun modelsBehave() {
        val q = SampleQuery("a", "b", "c", SampleStatus.VALIDADA, 1, 2)
        assertEquals(q, q.copy()); assertEquals(q.hashCode(), q.copy().hashCode()); assertTrue(q.toString().contains("a"))
        assertEquals("a", q.center); assertEquals("b", q.train); assertEquals("c", q.line)
        assertEquals(SampleStatus.VALIDADA, q.reviewStatus); assertEquals(1L, q.startDate); assertEquals(2L, q.endDate)
        val (c1, c2, c3, c4, c5, c6) = q
        assertEquals(listOf<Any?>("a", "b", "c", SampleStatus.VALIDADA, 1L, 2L), listOf(c1, c2, c3, c4, c5, c6))
        assertEquals(SampleQuery(), SampleQuery(null, null, null, null, null, null))
        assertFalse(q == q.copy(center = "z")); assertFalse(q.equals(null))
        val r = ReviewDto("VALIDADA", "c", "s")
        assertEquals(r, r.copy()); assertEquals("VALIDADA", r.reviewStatus); assertEquals("c", r.supervisorComment)
        assertEquals("s", r.supervisorName); assertTrue(r.toString().contains("VALIDADA")); assertEquals(r.hashCode(), r.copy().hashCode())
        assertEquals(setOf("Operador", "Supervisor", "Analista"), UserRole.entries.map { it.shortLabel }.toSet())
        assertEquals(UserRole.SUPERVISOR, UserRole.fromName("SUPERVISOR")); assertEquals(UserRole.OPERATOR, UserRole.fromName(null))
        assertEquals(SyncStatus.SINCRONIZADO, SyncStatus.valueOf("SINCRONIZADO")); assertEquals(3, SyncStatus.entries.size)
    }

    // ---------- Syncer ----------
    private class Store(vararg s: SampleEntity) : SyncStore {
        val items = s.toList(); val marks = mutableMapOf<Long, SyncStatus>()
        override suspend fun getUnsynced() = items
        override suspend fun markSyncStatus(id: Long, status: SyncStatus) { marks[id] = status }
    }
    private fun err(code: Int) = Response.error<Any>(code, "".toResponseBody())

    @Test fun syncerHandlesConflictFailureAndException() = runBlocking {
        val api = mockk<AquaSampleBackendApi>()
        coEvery { api.create(match { it.code == "A" }) } returns Response.success(null)
        coEvery { api.create(match { it.code == "B" }) } returns Response.error(409, "".toResponseBody())
        coEvery { api.create(match { it.code == "C" }) } returns Response.error(500, "".toResponseBody())
        coEvery { api.create(match { it.code == "D" }) } throws java.io.IOException("net")
        coEvery { api.create(match { it.code == "E" }) } returns Response.error(409, "".toResponseBody())
        coEvery { api.update(match { it == "B" }, any()) } returns Response.success(null)
        coEvery { api.update(match { it == "E" }, any()) } returns Response.error(500, "".toResponseBody())
        val store = Store(sample(1, "A"), sample(2, "B"), sample(3, "C"), sample(4, "D"), sample(5, "E"))
        val result = SampleSyncer(store, api).sync()
        assertEquals(2, result.synced); assertEquals(3, result.failed)
        assertEquals(SyncStatus.SINCRONIZADO, store.marks[2]); assertEquals(SyncStatus.ERROR_SINCRONIZACION, store.marks[5])
        assertEquals(2, result.copy().synced); assertTrue(result.toString().contains("synced"))
        val cancelling = mockk<AquaSampleBackendApi>()
        coEvery { cancelling.create(any()) } throws kotlinx.coroutines.CancellationException("cancel")
        try { SampleSyncer(Store(sample()), cancelling).sync(); fail() } catch (e: kotlinx.coroutines.CancellationException) { }
    }

    // ---------- Marine ----------
    private class Cache(var saved: com.aldemar.aquasample.data.remote.marine.MarineConditions? = null) : MarineCache {
        override suspend fun location() = MarineLocation.DEFAULT
        override suspend fun cached() = saved
        override suspend fun save(conditions: com.aldemar.aquasample.data.remote.marine.MarineConditions) { saved = conditions }
    }

    @Test fun marineRepositoryBranches() = runBlocking {
        val api = mockk<OpenMeteoMarineApi>()
        val cache = Cache()
        val repo = MarineRepository(api, cache) { 99L }
        coEvery { api.current(any(), any(), any(), any()) } returns MarineResponse(MarineCurrent("t", 1.0, 2.0, 3.0, 4.0))
        val ok = repo.load()
        assertFalse(ok.stale); assertEquals(99L, ok.conditions?.fetchedAt); assertEquals(4.0, ok.conditions?.seaTempC)
        coEvery { api.current(any(), any(), any(), any()) } returns MarineResponse(null)
        val empty = repo.load()
        assertTrue(empty.stale); assertEquals("Respuesta sin datos marinos", empty.error); assertEquals(99L, empty.conditions?.fetchedAt)
        coEvery { api.current(any(), any(), any(), any()) } throws RuntimeException()
        assertEquals("RuntimeException", repo.load().error)
        coEvery { api.current(any(), any(), any(), any()) } throws kotlinx.coroutines.CancellationException("c")
        try { repo.load(); fail() } catch (e: kotlinx.coroutines.CancellationException) { }
        assertTrue(MarineRepository.create(cache) .let { true })
        assertTrue(MarineRepository.create(cache, "http://localhost:1/").let { true })
        assertEquals(MarineResponse(null), MarineResponse())
        Unit
    }

    // ---------- AppSettings (DataStore on JVM) ----------
    @Test fun appSettingsPersistsValues() = runBlocking {
        val dir = Files.createTempDirectory("ds").toFile()
        val ctx = mockk<Context>(); every { ctx.applicationContext } returns ctx; every { ctx.filesDir } returns dir
        val s = AppSettings(ctx)
        assertEquals(AppSettings.DEFAULT_BACKEND_URL, s.backendUrl())
        s.setBackendUrl(" http://h:1/api "); assertEquals("http://h:1/api/", s.backendUrl())
        assertEquals("x/", AppSettings.normalizeBaseUrl("x")); assertEquals("x/", AppSettings.normalizeBaseUrl("x/"))
        assertEquals(MarineLocation.DEFAULT, s.location())
        s.setLocation(MarineLocation(1.0, 2.0, "n")); assertEquals(MarineLocation(1.0, 2.0, "n"), s.location())
        assertNull(s.cached())
        val cond = com.aldemar.aquasample.data.remote.marine.MarineConditions("n", 1.0, 2.0, 3L, 1.0, null, null, 4.0)
        s.save(cond); assertEquals(cond, s.cached())
        Unit
    }

    // ---------- PhotoStorage ----------
    private fun uriMock(text: String, authority: String?): Uri = mockk<Uri>().also {
        every { it.authority } returns authority; every { it.toString() } returns text
    }

    private fun photoEnv(dir: File, bytes: () -> InputStream?, width: Int = 10): Pair<PhotoStorage, ContentResolver> {
        val resolver = mockk<ContentResolver>()
        every { resolver.openInputStream(any()) } answers { bytes() }
        every { resolver.delete(any(), any(), any()) } returns 1
        val ctx = mockk<Context>()
        every { ctx.applicationContext } returns ctx; every { ctx.packageName } returns "pkg"
        every { ctx.filesDir } returns dir; every { ctx.contentResolver } returns resolver
        mockkStatic(Uri::class, FileProvider::class, BitmapFactory::class)
        every { Uri.parse(any()) } answers {
            val s = firstArg<String>(); uriMock(s, if (s.contains("pkg.fileprovider")) "pkg.fileprovider" else "other")
        }
        every { FileProvider.getUriForFile(any(), any(), any()) } answers {
            uriMock("content://pkg.fileprovider/" + thirdArg<File>().name, "pkg.fileprovider")
        }
        every { BitmapFactory.decodeStream(any(), any(), any()) } answers {
            thirdArg<BitmapFactory.Options>().apply { outWidth = width; outHeight = width }; null
        }
        return PhotoStorage(ctx) to resolver
    }

    @Test fun photoStorageHappyPathsAndGuards() = runBlocking {
        val dir = Files.createTempDirectory("ph").toFile()
        val (storage, resolver) = photoEnv(dir, { ByteArrayInputStream(ByteArray(20_000)) })
        val target = storage.prepareCapture("MUE-1/ weird*")
        assertTrue(target.file.exists()); assertTrue(target.file.name.startsWith("MUE-1__weird_"))
        assertTrue(target.uri.startsWith("content://pkg.fileprovider/"))
        assertTrue(storage.prepareCapture("").file.name.startsWith("muestra_"))
        storage.verify(target.uri)
        val stored = storage.importPhoto("content://gallery/1")
        assertTrue(stored.contains("pkg.fileprovider"))
        storage.discard(target.uri); verify { resolver.delete(any(), null, null) }
        storage.discard("content://other/1"); storage.discard(null)
        verify(exactly = 1) { resolver.delete(any(), any(), any()) }
        every { resolver.delete(any(), any(), any()) } throws SecurityException("x")
        storage.discard(target.uri) // swallowed
        Unit
    }

    @Test fun photoStorageRejectsBadImages() = runBlocking {
        val dir = Files.createTempDirectory("ph").toFile()
        val (invalid, _) = photoEnv(dir, { ByteArrayInputStream(ByteArray(10)) }, width = 0)
        try { invalid.verify("content://pkg.fileprovider/a"); fail() } catch (e: IllegalStateException) { }
        try { invalid.importPhoto("content://g/1"); fail() } catch (e: IllegalStateException) { }
        assertEquals(0, dir.resolve("images").listFiles()!!.count { it.name.startsWith("gallery_") })

        val (nullStream, _) = photoEnv(dir, { null })
        try { nullStream.verify("content://pkg.fileprovider/a"); fail() } catch (e: IllegalStateException) { }
        try { nullStream.importPhoto("content://g/1"); fail() } catch (e: IllegalStateException) { }

        val huge = object : InputStream() {
            override fun read() = 0
            override fun read(b: ByteArray, off: Int, len: Int) = len
        }
        val (big, _) = photoEnv(dir, { huge })
        try { big.importPhoto("content://g/1"); fail() } catch (e: IllegalStateException) { assertTrue(e.message!!.contains("25 MB")) }

        val blocker = File.createTempFile("blocker", ".tmp")
        val (broken, _) = photoEnv(blocker, { null })
        try { broken.prepareCapture("x"); fail() } catch (e: IllegalStateException) { }
        Unit
    }

    // ---------- FirebaseAuthRepository ----------
    private fun <T> task(result: T?, failure: Exception? = null): Task<T> {
        val t = mockk<Task<T>>()
        every { t.addOnCompleteListener(any<OnCompleteListener<T>>()) } answers {
            firstArg<OnCompleteListener<T>>().onComplete(t); t
        }
        every { t.isSuccessful } returns (failure == null)
        every { t.result } returns result
        every { t.exception } returns failure
        return t
    }

    private fun firebase(user: FirebaseUser?, claims: Map<String, Any> = mapOf("role" to "SUPERVISOR")): FirebaseAuth {
        val auth = mockk<FirebaseAuth>(relaxed = true)
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns auth
        every { auth.currentUser } returns user
        val token = mockk<GetTokenResult>(); every { token.claims } returns claims
        if (user != null) { val t = task(token); every { user.getIdToken(any()) } returns t }
        return auth
    }

    private fun fbUser(name: String?, email: String?): FirebaseUser {
        val u = mockk<FirebaseUser>(relaxed = true)
        every { u.uid } returns "uid"; every { u.displayName } returns name; every { u.email } returns email
        return u
    }

    @Test fun firebaseRepositoryRestoreLoginRegisterLogout() = runBlocking { kotlinx.coroutines.withTimeout(8000) {
        val repo = FirebaseAuthRepository()
        firebase(null); assertNull(repo.restore())
        val user = fbUser("Nombre", "a@b.c")
        val auth = firebase(user)
        assertEquals(AuthAccount("uid", "Nombre", "a@b.c", UserRole.SUPERVISOR), repo.restore())
        val okTask = task<AuthResult>(null)
        every { auth.signInWithEmailAndPassword(any(), any()) } returns okTask
        assertEquals("Nombre", repo.login("a@b.c", "p").name)
        val badTask = task<AuthResult>(null, IllegalStateException("bad"))
        every { auth.signInWithEmailAndPassword(any(), any()) } returns badTask
        try { repo.login("a@b.c", "p"); fail() } catch (e: IllegalStateException) { assertEquals("bad", e.message) }
        val cancelled = task<AuthResult>(null)
        every { cancelled.isSuccessful } returns false
        every { auth.signInWithEmailAndPassword(any(), any()) } returns cancelled
        try { repo.login("a@b.c", "p"); fail() } catch (e: IllegalStateException) { assertEquals("Solicitud cancelada", e.message) }
        val created = task<AuthResult>(null); val updated = task<Void>(null)
        every { auth.createUserWithEmailAndPassword(any(), any()) } returns created
        every { user.updateProfile(any()) } returns updated
        assertEquals("uid", repo.register("a@b.c", "p", "N").uid)
        repo.logout(); verify { auth.signOut() }
        // display name fallbacks
        val noName = fbUser(" ", "mail@x.y"); firebase(noName)
        assertEquals("mail", repo.restore()?.name)
        val anonymous = fbUser(null, null); firebase(anonymous, emptyMap())
        val acc = repo.restore()
        assertEquals("Usuario", acc?.name); assertEquals(UserRole.OPERATOR, acc?.role)
        val empty = firebase(null)
        val signedIn = task<AuthResult>(null); every { empty.signInWithEmailAndPassword(any(), any()) } returns signedIn
        try { repo.login("a", "b"); fail() } catch (e: IllegalStateException) { assertEquals("Sesión no disponible", e.message) }
        Unit }
    }

    // ---------- Worker / Scheduler ----------
    @Test fun schedulerEnqueuesUniqueWork() {
        val wm = mockk<WorkManager>(relaxed = true)
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any<Context>()) } returns wm
        val ctx = mockk<Context>(relaxed = true)
        SyncScheduler.schedulePeriodic(ctx); SyncScheduler.syncNow(ctx)
        verify { wm.enqueueUniquePeriodicWork("sync_samples_periodic", ExistingPeriodicWorkPolicy.KEEP, any()) }
        val name = slot<String>()
        verify { wm.enqueueUniqueWork(capture(name), ExistingWorkPolicy.APPEND_OR_REPLACE, any<androidx.work.OneTimeWorkRequest>()) }
        assertEquals("sync_samples_now", name.captured)
    }

    private fun workerWith(api: AquaSampleBackendApi, unsynced: List<SampleEntity>): SyncSamplesWorker {
        coEvery { api.list() } returns emptyList()
        val app = mockk<AquaSampleApp>()
        val settings = mockk<AppSettings>(); coEvery { settings.backendUrl() } returns "http://localhost/api/"
        val repo = mockk<SampleRepository>()
        coEvery { repo.getUnsynced() } returns unsynced
        coEvery { repo.markSyncStatus(any(), any()) } returns Unit
        coEvery { repo.finishUpload(any(), any()) } returns Unit
        every { app.settings } returns settings; every { app.repository } returns repo
        every { app.applicationContext } returns app
        mockkObject(BackendClient); every { BackendClient.create(any(), any()) } returns api
        val user = fbUser("Operador", "op@example.test")
        firebase(user)
        val token = mockk<GetTokenResult>()
        every { token.token } returns "synthetic-test-token"
        every { user.getIdToken(any()) } returns task(token)
        return SyncSamplesWorker(app, mockk<WorkerParameters>(relaxed = true))
    }

    @Test fun workerReturnsSuccessOrRetry() = runBlocking {
        val ok = mockk<AquaSampleBackendApi>(); coEvery { ok.create(any()) } returns Response.success(null)
        assertTrue(workerWith(ok, listOf(sample())).doWork() is ListenableWorker.Result.Success)
        val bad = mockk<AquaSampleBackendApi>(); coEvery { bad.create(any()) } throws java.io.IOException("x")
        assertTrue(workerWith(bad, listOf(sample())).doWork() is ListenableWorker.Result.Retry)
        val worker = workerWith(ok, emptyList())
        firebase(null)
        assertTrue(worker.doWork() is ListenableWorker.Result.Retry)
    }
}
