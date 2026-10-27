package com.aldemar.aquasample

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DemoIsolationTest {
    @Test fun demonstrationSampleNeverAppearsInRealDatabase() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<AquaSampleApp>()
        val code = "DEMO-TEST-${UUID.randomUUID()}"
        val id = app.demoRepository.insertSample(SampleEntity(code = code, centerName = "Prueba",
            trainNumber = "1", lineNumber = "1", sampleDate = "26-10-2026", sectionLengthMeters = 1.0,
            operatorName = "Prueba", musselCount = 0, observations = "Prueba de aislamiento"))
        assertNotNull(app.demoDatabase.sampleDao().getSampleByIdOnce(id))
        assertNull(app.database.sampleDao().findIdByCode(code))
        assertNotEquals(app.demoDatabase.openHelper.databaseName, app.database.openHelper.databaseName)
    }
}
