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
        val id = app.demoRepository.insertSample(SampleEntity(code = code, center = "Prueba",
            train = "1", line = "1", timestamp = System.currentTimeMillis(), sectionLengthCm = 100,
            operatorName = "Prueba", manualCount = 0, observations = "Prueba de aislamiento"))
        assertNotNull(app.demoDatabase.sampleDao().getSampleByIdOnce(id))
        assertNull(app.database.sampleDao().findIdByCode(code))
        assertNotEquals(app.demoDatabase.openHelper.databaseName, app.database.openHelper.databaseName)
    }
}
