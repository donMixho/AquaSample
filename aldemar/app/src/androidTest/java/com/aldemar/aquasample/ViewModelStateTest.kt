package com.aldemar.aquasample

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aldemar.aquasample.data.local.AquaSampleDatabase
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.repository.PhotoStorage
import com.aldemar.aquasample.data.repository.SampleRepository
import com.aldemar.aquasample.ui.viewmodel.*
import java.io.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewModelStateTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun main(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)

    @Test fun serializedDraftRestoresFieldsAndPendingPhotoWithSameIdentity() {
        val db = Room.inMemoryDatabaseBuilder(context, AquaSampleDatabase::class.java).build()
        val store = ViewModelStore()
        try {
            main {
                val repository = SampleRepository(db.sampleDao())
                val original = SampleCreateViewModel(repository, PhotoStorage(context), SavedStateHandle())
                store.put("original", original)
                original.updateTrain("Tren 17")
                original.updateLine("Línea 8")
                original.updateCount("23")
                original.setOperator("Matías")
                val draft = original.uiState.value.copy(pendingPhotoUri = "content://test/image", isSaving = true)
                val bytes = ByteArrayOutputStream().also { buffer ->
                    ObjectOutputStream(buffer).use { it.writeObject(draft) }
                }.toByteArray()
                val restoredDraft = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() } as CreateSampleUiState
                val restored = SampleCreateViewModel(repository, PhotoStorage(context), SavedStateHandle(mapOf("draft" to restoredDraft)))
                store.put("restored", restored)
                assertEquals("Tren 17", restored.uiState.value.trainNumber)
                assertEquals("Línea 8", restored.uiState.value.lineNumber)
                assertEquals(23, restored.uiState.value.musselCount)
                assertEquals("Matías", restored.uiState.value.operatorName)
                assertEquals(draft.code, restored.uiState.value.code)
                assertEquals(draft.pendingPhotoUri, restored.uiState.value.pendingPhotoUri)
                assertFalse(restored.uiState.value.isSaving)
            }
        } finally { main { store.clear() }; db.close() }
    }

    @Test fun operatorCannotApplyReviewAndSupervisorNeedsObservationReason() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AquaSampleDatabase::class.java).build()
        val store = ViewModelStore()
        try {
            val repository = SampleRepository(db.sampleDao())
            val id = repository.insertSample(SampleEntity(code="ROLE-TEST", centerName="Huar", trainNumber="1",
                lineNumber="1", sampleDate="22-10-2026", sectionLengthMeters=1.0,
                operatorName="Operador", musselCount=0, observations=""))
            lateinit var vm: SupervisorReviewViewModel
            main {
                vm = SupervisorReviewViewModel(repository)
                store.put("review", vm)
                vm.loadSample(id)
            }
            withTimeout(10000) { vm.uiState.first { it.sample != null } }
            main {
                vm.applyReview(SampleStatus.VALIDADO, "Operador", UserRole.OPERADOR) { fail("Unauthorized success") }
                assertNotNull(vm.uiState.value.errorMessage)
            }
            assertEquals("PENDIENTE", db.sampleDao().getSampleByIdOnce(id)!!.status)
            main {
                vm.applyReview(SampleStatus.OBSERVADO, "Supervisora", UserRole.SUPERVISOR) { fail("Empty reason accepted") }
                assertEquals("Explica el motivo de la observación.", vm.uiState.value.errorMessage)
                vm.updateComment("Repetir conteo")
                vm.applyReview(SampleStatus.OBSERVADO, "Supervisora", UserRole.SUPERVISOR) { }
            }
            withTimeout(10000) { vm.uiState.first { it.isUpdated } }
            val stored = db.sampleDao().getSampleByIdOnce(id)!!
            assertEquals("OBSERVADO", stored.status)
            assertEquals("Repetir conteo", stored.supervisorComment)
        } finally { main { store.clear() }; db.close() }
    }
}
