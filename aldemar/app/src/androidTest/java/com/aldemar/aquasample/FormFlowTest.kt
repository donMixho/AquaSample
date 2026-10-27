package com.aldemar.aquasample

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquasample.ui.viewmodel.SampleCreateViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun createSampleSurvivesActivityRecreationAndAppearsInHistory() {
        compose.onNodeWithText("Ingresar en Modo Demostración (Offline)").performScrollTo().performClick()
        compose.onNodeWithText("Nueva Muestra", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Tren / Módulo *").performTextInput("Tren PRUEBA")
        compose.onNodeWithText("Línea / Cuerda *").performTextInput("Línea PRUEBA")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Tren PRUEBA").assertExists()
        compose.onNodeWithText("Línea PRUEBA").assertExists()
        compose.onNodeWithText("Siguiente: Evidencia Fotográfica ➔").performScrollTo().performClick()
        val app = compose.activity.application as AquaSampleApp
        val source=runBlocking { app.photoStorage.prepareCamera() }
        compose.activity.contentResolver.openOutputStream(Uri.parse(source))!!.use {
            val bitmap=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888)
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,it); bitmap.recycle()
        }
        lateinit var vm: SampleCreateViewModel
        compose.runOnIdle {
            vm=ViewModelProvider(compose.activity).get("demo-create", SampleCreateViewModel::class.java)
            vm.importPhoto(source)
        }
        compose.waitUntil(10000) { vm.uiState.value.photoPath != null && !vm.uiState.value.isPhotoBusy }
        runBlocking { app.photoStorage.discard(source) }
        compose.onNodeWithText("Siguiente: Conteo y Observaciones ➔").performScrollTo().performClick()
        compose.onNodeWithText("O ingrese número exacto").performTextReplacement("23")
        compose.onNodeWithText("Siguiente: Resumen de Muestra ➔").performScrollTo().performClick()
        compose.onNodeWithText("23 choritos").assertExists()
        val code=vm.uiState.value.code
        compose.onNodeWithText("Guardar muestra").performScrollTo().performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Nueva Muestra", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Buscar por código, centro o línea...").performTextInput(code)
        compose.onNode(hasText(code) and hasSetTextAction().not()).assertExists()
        val saved=runBlocking {
            val id=app.demoDatabase.sampleDao().findIdByCode(code)!!
            app.demoDatabase.sampleDao().getSampleByIdOnce(id)!!
        }
        assertEquals(23,saved.musselCount)
        assertNotNull(saved.photoPath)
        // Opens the detail as operator: review actions must be absent.
        compose.onNode(hasText(code) and hasSetTextAction().not()).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Consulta de la muestra. El dictamen corresponde al supervisor.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Validar",useUnmergedTree=true).assertDoesNotExist()
        compose.onNodeWithText("Observar",useUnmergedTree=true).assertDoesNotExist()
    }
}
