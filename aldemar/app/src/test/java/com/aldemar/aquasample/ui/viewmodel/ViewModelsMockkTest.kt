package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.repository.PhotoStorage
import com.aldemar.aquasample.data.repository.SampleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

private fun entity(id: Long = 1, code: String = "MUE-$id", train: String = "T1", line: String = "L1",
                   status: SampleStatus = SampleStatus.PENDIENTE_REVISION, center: String = "Centro Huar") =
    SampleEntity(id = id, code = code, center = center, train = train, line = line, sectionLengthCm = 100,
        timestamp = 1000L * id, operatorName = "Op", manualCount = 5, observations = "ok", reviewStatus = status,
        supervisorComment = "coment")

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsMockkTest {
    private val store = ViewModelStore()
    @Before fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun TestScope.collect(f: StateFlow<*>): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { f.collect { } }

    // ---------- Analytics ----------
    @Test fun analyticsExposesSamplesAndFallsBackToEmptyOnError() = runTest {
        val repo = mockk<SampleRepository>()
        every { repo.allSamples } returns flowOf(listOf(entity()))
        val vm = AnalyticsViewModel(repo); store.put("a", vm)
        collect(vm.samples)
        assertEquals(1, vm.samples.value.size)

        val bad = mockk<SampleRepository>()
        every { bad.allSamples } returns flow { throw IllegalStateException("db") }
        val vm2 = AnalyticsViewModel(bad); store.put("b", vm2)
        collect(vm2.samples)
        assertTrue(vm2.samples.value.isEmpty())
    }

    // ---------- List ----------
    private fun listRepo(samples: Flow<List<SampleEntity>>): SampleRepository {
        val repo = mockk<SampleRepository>()
        every { repo.allSamples } returns samples
        every { repo.allCenters } returns flowOf(listOf(CenterEntity(name = "Centro Huar", location = "x")))
        return repo
    }

    @Test fun listViewModelFiltersAndMessages() = runTest {
        val a = entity(1, train = "T1", line = "L1")
        val b = entity(2, train = "t2", line = "L2", status = SampleStatus.VALIDADA, center = "Otro")
        val vm = SampleListViewModel(listRepo(flowOf(listOf(a, b))), SavedStateHandle()); store.put("l", vm)
        listOf(vm.uiState, vm.centers, vm.trains, vm.lines, vm.selectedStatusFilter).forEach { collect(it) }
        assertEquals(2, vm.uiState.value.samples.size); assertFalse(vm.uiState.value.isLoading)
        assertEquals(listOf("T1", "t2"), vm.trains.value); assertEquals(listOf("L1", "L2"), vm.lines.value)
        assertEquals(1, vm.centers.value.size)

        vm.showMessage("hola"); assertEquals("hola", vm.message.value); vm.consumeMessage(); assertNull(vm.message.value)
        vm.updateSearchQuery("x".repeat(150)); assertEquals(100, vm.searchQuery.value.length)
        vm.updateSearchQuery("")
        vm.setStatusFilter(SampleStatus.VALIDADA); assertEquals(SampleStatus.VALIDADA, vm.selectedStatusFilter.value)
        assertEquals(listOf(b), vm.uiState.value.samples)
        vm.setStatusFilter(null)
        vm.setCenterFilter("Centro Huar"); assertEquals(listOf(a), vm.uiState.value.samples)
        assertEquals("Centro Huar", vm.selectedCenterFilter.value)
        vm.setCenterFilter(null); vm.setTrainFilter("T2"); assertEquals(listOf(b), vm.uiState.value.samples)
        assertEquals("T2", vm.selectedTrain.value)
        vm.setTrainFilter(null); vm.setLineFilter("L1"); assertEquals(listOf(a), vm.uiState.value.samples)
        assertEquals("L1", vm.selectedLine.value)
        vm.setLineFilter(null); vm.setStartDate(2000L); vm.setEndDate(2000L)
        assertEquals(listOf(b), vm.uiState.value.samples)
        assertEquals(2000L, vm.startDate.value); assertEquals(2000L, vm.endDate.value)
        vm.clearFilters(); assertEquals(2, vm.uiState.value.samples.size)
        vm.retryLoad(); assertEquals(2, vm.uiState.value.samples.size)
    }

    @Test fun listViewModelRestoresStatusFromSavedStateAndReportsErrors() = runTest {
        val vm = SampleListViewModel(listRepo(flow { throw IllegalStateException("x") }),
            SavedStateHandle(mapOf("status" to "VALIDADA"))); store.put("l", vm)
        collect(vm.uiState)
        assertEquals(SampleStatus.VALIDADA, vm.selectedStatusFilter.value)
        assertNotNull(vm.uiState.value.error); assertFalse(vm.uiState.value.isLoading)

        val failing = mockk<SampleRepository>()
        every { failing.allSamples } returns flowOf(emptyList())
        every { failing.allCenters } returns flow { throw IllegalStateException("c") }
        val vm2 = SampleListViewModel(failing, SavedStateHandle()); store.put("l2", vm2)
        collect(vm2.centers); assertTrue(vm2.centers.value.isEmpty())

        val bad = mockk<SampleRepository>()
        every { bad.allSamples } returns flow { throw IllegalStateException("s") }
        every { bad.allCenters } returns flowOf(emptyList())
        val vm3 = SampleListViewModel(bad, SavedStateHandle()); store.put("l3", vm3)
        collect(vm3.trains); collect(vm3.lines)
        assertTrue(vm3.trains.value.isEmpty()); assertTrue(vm3.lines.value.isEmpty())
    }

    // ---------- Review ----------
    @Test fun reviewLoadsAndValidates() = runTest {
        val repo = mockk<SampleRepository>()
        val source = MutableStateFlow<SampleEntity?>(entity(1))
        every { repo.getSampleById(1) } returns source
        every { repo.getSampleById(2) } returns flowOf(null)
        every { repo.getSampleById(3) } returns flow { throw IllegalStateException("x") }
        coEvery { repo.updateStatus(any(), any(), any(), any()) } returns Unit
        val vm = SupervisorReviewViewModel(repo); store.put("r", vm)
        vm.applyReview(SampleStatus.VALIDADA, "Sup", UserRole.SUPERVISOR) { error("no sample yet") }
        vm.loadSample(1)
        assertEquals("coment", vm.uiState.value.supervisorComment)
        vm.loadSample(1) // already active: no-op
        var ok = 0
        vm.applyReview(SampleStatus.VALIDADA, "Sup", UserRole.OPERATOR) { ok++ }
        assertNotNull(vm.uiState.value.errorMessage)
        vm.applyReview(SampleStatus.VALIDADA, " ", UserRole.SUPERVISOR) { ok++ }
        assertEquals("Falta el nombre del supervisor.", vm.uiState.value.errorMessage)
        vm.applyReview(SampleStatus.CORREGIDA, "Sup", UserRole.SUPERVISOR) { ok++ }
        assertEquals("Selecciona Validar u Observar.", vm.uiState.value.errorMessage)
        vm.updateComment(" ")
        vm.applyReview(SampleStatus.OBSERVADA, "Sup", UserRole.SUPERVISOR) { ok++ }
        assertEquals("Explica el motivo de la observación.", vm.uiState.value.errorMessage)
        assertEquals(0, ok)
        vm.updateComment("x".repeat(3000)); assertEquals(2001, vm.uiState.value.supervisorComment.length)
        vm.updateComment("")
        source.value = entity(1) // commentEdited keeps the draft
        vm.applyReview(SampleStatus.VALIDADA, " Sup ", UserRole.SUPERVISOR) { ok++ }
        assertEquals(1, ok); assertTrue(vm.uiState.value.isUpdated)
        coVerify { repo.updateStatus(1, SampleStatus.VALIDADA, "Muestra revisada y validada.", "Sup") }
        vm.loadSample(2); assertEquals("La muestra no existe.", vm.uiState.value.errorMessage)
        vm.loadSample(3); assertEquals("No se pudo cargar la muestra.", vm.uiState.value.errorMessage)
    }

    @Test fun reviewSaveFailureAndDoubleClick() = runTest {
        val repo = mockk<SampleRepository>()
        every { repo.getSampleById(1) } returns flowOf(entity(1))
        val gate = CompletableDeferred<Unit>()
        coEvery { repo.updateStatus(any(), any(), any(), any()) } coAnswers { gate.await(); throw IllegalStateException("db") }
        val vm = SupervisorReviewViewModel(repo); store.put("r", vm)
        vm.loadSample(1)
        var ok = 0
        vm.applyReview(SampleStatus.VALIDADA, "S", UserRole.SUPERVISOR) { ok++ }
        assertTrue(vm.uiState.value.isSaving)
        vm.applyReview(SampleStatus.VALIDADA, "S", UserRole.SUPERVISOR) { ok++ } // ignored while saving
        gate.complete(Unit)
        assertEquals("No se pudo guardar el dictamen. Reintenta.", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSaving); assertEquals(0, ok)
    }

    // ---------- Create ----------
    private class Fixture {
        val repo = mockk<SampleRepository>()
        val photos = mockk<PhotoStorage>()
        init {
            every { repo.allCenters } returns flowOf(emptyList())
            coEvery { repo.insertSample(any()) } returns 7L
            coEvery { repo.findIdByCode(any()) } returns null
            coEvery { photos.discard(any()) } returns Unit
            coEvery { photos.verify(any()) } returns Unit
            coEvery { photos.importPhoto(any()) } returns "content://stored/1"
            coEvery { photos.prepareCapture(any()) } returns PhotoStorage.CaptureTarget(File("cap.jpg"), "content://cap/1")
        }
        val handle = SavedStateHandle()
        fun vm() = SampleCreateViewModel(repo, photos, handle)
    }

    private fun SampleCreateViewModel.fillValid() {
        setOperator("Op"); updateCenter("Centro"); updateTrain("T"); updateLine("L"); updateConcession("C")
        updateSectionLength("100"); updateObservations("obs"); importPhoto("content://g/1")
    }

    @Test fun createFieldEditingAndMarkers() = runTest {
        val fx = Fixture(); val vm = fx.vm(); store.put("c", vm)
        collect(vm.centers)
        vm.setOperator("o".repeat(200)); assertEquals(101, vm.uiState.value.operatorName.length)
        vm.updateCenter("c"); vm.updateTrain("t"); vm.updateLine("l"); vm.updateConcession("k"); vm.updateObservations("o")
        vm.updateSectionLength("abc"); assertTrue("length" in vm.uiState.value.fieldErrors)
        vm.updateSectionLength("120"); assertFalse("length" in vm.uiState.value.fieldErrors)
        vm.updateCount("x"); assertTrue("count" in vm.uiState.value.fieldErrors)
        vm.updateCount("5"); assertEquals(5, vm.uiState.value.musselCount)
        vm.addMarker(0.5f, 0.5f); vm.addMarker(2f, 2f) // out of range ignored
        assertEquals(1, vm.uiState.value.markers.size); assertEquals(6, vm.uiState.value.musselCount)
        vm.incrementCount(3); assertEquals(9, vm.uiState.value.musselCount)
        vm.incrementCount(-100); assertEquals(0, vm.uiState.value.musselCount)
        vm.addMarker(0.1f, 0.2f); vm.undoLastMarker(); assertEquals(1, vm.uiState.value.markers.size)
        vm.clearMarkers(); vm.undoLastMarker(); assertTrue(vm.uiState.value.markers.isEmpty())
        vm.updateCount("1000000"); vm.addMarker(0.3f, 0.3f)
        assertEquals(0, vm.uiState.value.markers.size) // would exceed MAX_COUNT
        vm.updateCount("zzz"); assertFalse(vm.validateCount())
    }

    @Test fun createValidationAndPhotoFlow() = runTest {
        val fx = Fixture(); val vm = fx.vm(); store.put("c", vm)
        assertFalse(vm.validatePhoto()); assertNotNull(vm.uiState.value.photoErrorMessage)
        assertFalse(vm.validateStep1()); assertTrue(vm.uiState.value.fieldErrors.isNotEmpty())
        var ready: File? = null
        vm.prepareCapture { ready = it }
        assertEquals(File("cap.jpg"), ready); assertTrue(vm.uiState.value.isPhotoBusy)
        vm.prepareCapture { error("busy") } // ignored while busy
        vm.captureFinished(true); assertFalse(vm.uiState.value.isPhotoBusy)
        assertFalse(vm.validatePhoto()) // pending photo not confirmed
        vm.confirmPendingPhoto()
        assertEquals("content://cap/1", vm.uiState.value.photoPath); assertTrue(vm.validatePhoto())
        vm.confirmPendingPhoto() // nothing pending
        vm.addMarker(0.5f, 0.5f); vm.addMarker(0.2f, 0.2f)
        vm.importPhoto("content://other") // replaces photo: markers become adjustment
        assertEquals("content://stored/1", vm.uiState.value.photoPath)
        assertTrue(vm.uiState.value.markers.isEmpty()); assertEquals(2, vm.uiState.value.musselCount)
        vm.importPhoto("content://other") // same path again: untouched
        coVerify { fx.photos.discard("content://cap/1") }
        vm.prepareCapture { }; vm.captureFinished(false)
        assertNotNull(vm.uiState.value.photoErrorMessage); assertNull(vm.uiState.value.pendingPhotoUri)
        vm.prepareCapture { }; vm.captureFinished(false, "msg"); assertEquals("msg", vm.uiState.value.photoErrorMessage)
        vm.setPhotoError("e"); assertEquals("e", vm.uiState.value.photoErrorMessage)
        vm.discardPendingPhoto(); assertNull(vm.uiState.value.photoErrorMessage)
    }

    @Test fun createPhotoFailures() = runTest {
        val fx = Fixture(); val vm = fx.vm(); store.put("c", vm)
        coEvery { fx.photos.prepareCapture(any()) } throws IllegalStateException("disk")
        vm.prepareCapture { error("never") }
        assertFalse(vm.uiState.value.isPhotoBusy); assertNull(vm.uiState.value.pendingPhotoUri)
        assertTrue(vm.uiState.value.photoErrorMessage!!.contains("No se pudo preparar"))
        coEvery { fx.photos.importPhoto(any()) } throws IllegalStateException("grande")
        vm.importPhoto("content://x"); assertEquals("grande", vm.uiState.value.photoErrorMessage)
        coEvery { fx.photos.importPhoto(any()) } throws IllegalStateException()
        vm.importPhoto("content://x"); assertEquals("No se pudo guardar la imagen.", vm.uiState.value.photoErrorMessage)
    }

    @Test fun createSaveHappyPathIsIdempotent() = runTest {
        val fx = Fixture(); val vm = fx.vm(); store.put("c", vm)
        var saved = -1L
        vm.saveSample { saved = it } // invalid: nothing yet
        assertNotNull(vm.uiState.value.errorMessage); assertEquals(-1L, saved)
        vm.fillValid(); vm.updateCount("12")
        vm.saveSample { saved = it }
        assertEquals(7L, saved); assertTrue(vm.uiState.value.isSaved)
        saved = -1; vm.saveSample { saved = it }; assertEquals(7L, saved)
        coVerify(exactly = 1) { fx.repo.insertSample(match { it.manualCount == 12 && it.concession == "C" }) }
        vm.reset(); assertFalse(vm.uiState.value.isSaved)
    }

    @Test fun createSaveFailureAndBlankFields() = runTest {
        val fx = Fixture(); val vm = fx.vm(); store.put("c", vm)
        vm.fillValid(); vm.updateConcession(""); vm.updateObservations("")
        coEvery { fx.repo.insertSample(any()) } throws IllegalStateException("boom")
        vm.saveSample { error("no") }
        assertTrue(vm.uiState.value.errorMessage!!.contains("boom")); assertFalse(vm.uiState.value.isSaving)
        coEvery { fx.repo.insertSample(any()) } returns 9L
        vm.saveSample { }
        coVerify { fx.repo.insertSample(match { it.concession == null && it.observations == "Sin observaciones adicionales." }) }
    }

    @Test fun createResetDiscardsUncommittedPhotoOnly() = runTest {
        val fx = Fixture(); val vm = fx.vm(); store.put("c", vm)
        vm.fillValid(); vm.reset()
        coVerify { fx.photos.discard("content://stored/1") }
        val fx2 = Fixture(); val vm2 = fx2.vm(); store.put("c2", vm2)
        vm2.fillValid(); coEvery { fx2.repo.findIdByCode(any()) } returns 3L; vm2.reset()
        coVerify(exactly = 0) { fx2.photos.discard("content://stored/1") }
        val fx3 = Fixture(); val vm3 = fx3.vm(); store.put("c3", vm3)
        vm3.fillValid(); coEvery { fx3.repo.findIdByCode(any()) } throws IllegalStateException("db"); vm3.reset()
        coVerify(exactly = 0) { fx3.photos.discard("content://stored/1") }
    }
}
