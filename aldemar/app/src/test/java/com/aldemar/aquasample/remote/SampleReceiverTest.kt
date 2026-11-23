package com.aldemar.aquasample.remote

import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.*
import com.aldemar.aquasample.data.remote.backend.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SampleReceiverTest {
    private fun sample() = SampleEntity(id=7, code="A", center="Huar", train="1", line="2",
        sectionLengthCm=100, timestamp=1, operatorName="Operador", manualCount=20,
        observations="ok", photoPath="content://private/photo", syncStatus=SyncStatus.SINCRONIZADO)

    @Test fun supervisorDecisionArrivesAndLocalPhotoIsPreserved() = runBlocking {
        val local = sample()
        val api = mockk<AquaSampleBackendApi>()
        val dao = mockk<SampleDao>()
        coEvery { api.list() } returns listOf(local.copy(reviewStatus=SampleStatus.VALIDADA).toDto())
        coEvery { dao.findIdByCode("A") } returns 7
        coEvery { dao.getSampleByIdOnce(7) } returns local
        coEvery { dao.mergeRemote(any()) } coAnswers { callOriginal() }
        val saved = slot<SampleEntity>()
        coEvery { dao.updateSample(capture(saved)) } just Runs
        assertEquals(1, SampleReceiver(api, dao::mergeRemote).receive())
        assertEquals(SampleStatus.VALIDADA, saved.captured.reviewStatus)
        assertEquals(local.photoPath, saved.captured.photoPath)
        assertEquals(local.id, saved.captured.id)
    }

    @Test fun pendingLocalEditsAreNotOverwritten() = runBlocking {
        val dao = mockk<SampleDao>()
        coEvery { dao.findIdByCode("A") } returns 7
        coEvery { dao.getSampleByIdOnce(7) } returns sample().copy(syncStatus=SyncStatus.PENDIENTE_SINCRONIZACION)
        coEvery { dao.mergeRemote(any()) } coAnswers { callOriginal() }
        assertFalse(dao.mergeRemote(sample().copy(manualCount=99)))
        coVerify(exactly=0) { dao.updateSample(any()) }
    }

    @Test fun editDuringUploadRemainsPending() = runBlocking {
        val dao = mockk<SampleDao>()
        val snapshot = sample().copy(syncStatus=SyncStatus.PENDIENTE_SINCRONIZACION)
        coEvery { dao.getSampleByIdOnce(7) } returns snapshot.copy(manualCount=42)
        coEvery { dao.finishUpload(any(), any()) } coAnswers { callOriginal() }
        dao.finishUpload(snapshot, SyncStatus.SINCRONIZADO)
        coVerify(exactly=0) { dao.updateSyncStatus(any(), any()) }
    }
}
