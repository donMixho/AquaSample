package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.MarkerSerializer
import com.aldemar.aquasample.data.model.SampleCount
import com.aldemar.aquasample.data.model.SampleMarker
import com.aldemar.aquasample.data.repository.PhotoStorage
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.Serializable
import java.util.UUID

data class CreateSampleUiState(
    val centerName: String = "Centro Huar",
    val trainNumber: String = "",
    val lineNumber: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sampleDate: String = SampleFormat.dateTime(timestamp),
    val concession: String = "",
    val sectionLengthCm: String = "100",
    val operatorName: String = "",
    val pendingPhotoUri: String? = null,
    val photoPath: String? = null,
    val photoErrorMessage: String? = null,
    val isPhotoBusy: Boolean = false,
    /** Texto del campo "número exacto"; refleja siempre [musselCount] salvo mientras se edita. */
    val countText: String = "0",
    /** Marcas puestas sobre la foto (coordenadas normalizadas). */
    val markers: List<SampleMarker> = emptyList(),
    /** Corrección manual: total = marcas + ajuste. */
    val manualAdjustment: Int = 0,
    val observations: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedId: Long? = null,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val code: String = "MUE-${UUID.randomUUID()}"
) : Serializable {
    /** Conteo final = marcas + ajuste manual (es lo que se guarda como manualCount). */
    val musselCount: Int get() = SampleCount.total(markers.size, manualAdjustment)
}

class SampleCreateViewModel(
    private val repository: SampleRepository,
    private val photos: PhotoStorage,
    private val savedState: SavedStateHandle
) : ViewModel() {
    init {
        val restored = savedState.get<CreateSampleUiState>("draft") ?: CreateSampleUiState()
        savedState["draft"] = restored.copy(isSaving = false, isPhotoBusy = false)
    }
    val uiState = savedState.getStateFlow("draft", CreateSampleUiState())
    private var state: CreateSampleUiState
        get() = uiState.value
        set(value) { savedState["draft"] = value }
    val centers = repository.allCenters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setOperator(value: String) { state = state.copy(operatorName = value.take(101), fieldErrors = state.fieldErrors - "operator") }
    fun updateCenter(value: String) { state = state.copy(centerName = value.take(101), fieldErrors = state.fieldErrors - "center") }
    fun updateTrain(value: String) { state = state.copy(trainNumber = value.take(101), fieldErrors = state.fieldErrors - "train") }
    fun updateLine(value: String) { state = state.copy(lineNumber = value.take(101), fieldErrors = state.fieldErrors - "line") }
    fun updateConcession(value: String) { state = state.copy(concession = value.take(101), fieldErrors = state.fieldErrors - "concession") }
    fun updateSectionLength(value: String) {
        val text = value.take(8)
        // Valida mientras se escribe, para que el mensaje aparezca bajo el campo de inmediato.
        val error = SampleValidation.validateLength(text)
        state = state.copy(sectionLengthCm = text, fieldErrors = (state.fieldErrors - "length") +
            if (error == null) emptyMap() else mapOf("length" to error))
    }
    fun updateObservations(value: String) { state = state.copy(observations = value.take(2000)) }

    /** Edición directa del total: se traduce a ajuste manual y las marcas se conservan. */
    fun updateCount(value: String) {
        val text = value.take(16)
        val error = SampleValidation.validateCount(text)
        val target = text.trim().toIntOrNull()
        state = state.copy(
            countText = text,
            manualAdjustment = if (error == null && target != null)
                SampleCount.adjustmentFor(target, state.markers.size) else state.manualAdjustment,
            fieldErrors = (state.fieldErrors - "count") + if (error == null) emptyMap() else mapOf("count" to error)
        )
    }

    /** Agrega una marca en coordenadas normalizadas (0..1 sobre la imagen original). */
    fun addMarker(x: Float, y: Float) {
        val marker = SampleMarker.ofOrNull(x, y) ?: return
        val s = state
        if (s.markers.size >= SampleCount.MAX_MARKERS) return
        if (s.markers.size + 1 + s.manualAdjustment > SampleCount.MAX_COUNT) return
        applyMarkers(s.markers + marker)
    }
    fun undoLastMarker() {
        val s = state
        if (s.markers.isNotEmpty()) applyMarkers(s.markers.dropLast(1))
    }
    fun clearMarkers() = applyMarkers(emptyList())

    /** Cambia las marcas conservando el ajuste manual (acotado) y sincroniza el texto del contador. */
    private fun applyMarkers(markers: List<SampleMarker>) {
        val adjustment = SampleCount.normalizeAdjustment(markers.size, state.manualAdjustment)
        state = state.copy(markers = markers, manualAdjustment = adjustment,
            countText = SampleCount.total(markers.size, adjustment).toString(),
            fieldErrors = state.fieldErrors - "count")
    }
    fun incrementCount(amount: Int) {
        val next = (state.musselCount.toLong() + amount).coerceIn(0, SampleValidation.MAX_COUNT.toLong())
        updateCount(next.toString())
    }
    fun validateStep1(): Boolean {
        val errors = SampleValidation.validateLocation(state.centerName, state.trainNumber,
            state.lineNumber, state.operatorName, state.sectionLengthCm, state.concession)
        state = state.copy(fieldErrors = errors, errorMessage = null)
        return errors.isEmpty()
    }
    fun validateCount(): Boolean {
        updateCount(state.countText)
        return "count" !in state.fieldErrors
    }
    fun validatePhoto(): Boolean {
        if (state.photoPath == null || state.pendingPhotoUri != null || state.isPhotoBusy) {
            setPhotoError("Toma o elige una foto y confírmala antes de continuar.")
            return false
        }
        return true
    }
    fun setPhotoError(message: String) { state = state.copy(photoErrorMessage = message) }
    /**
     * Reserva el archivo de la foto (filesDir/images, nombre con el código de la muestra) y lo entrega
     * a CameraX. Mientras dura la captura [CreateSampleUiState.isPhotoBusy] bloquea otras acciones;
     * la pantalla debe terminar siempre con [captureFinished].
     */
    fun prepareCapture(onReady: (File) -> Unit) {
        if (state.isPhotoBusy) return
        state = state.copy(isPhotoBusy = true, photoErrorMessage = null)
        viewModelScope.launch {
            try {
                photos.discard(state.pendingPhotoUri)
                val target = photos.prepareCapture(state.code)
                state = state.copy(pendingPhotoUri = target.uri)
                onReady(target.file)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                photos.discard(state.pendingPhotoUri)
                state = state.copy(pendingPhotoUri = null, isPhotoBusy = false,
                    photoErrorMessage = "No se pudo preparar la captura. Puedes usar la galería.")
            }
        }
    }
    fun captureFinished(success: Boolean, message: String? = null) {
        if (success) { state = state.copy(isPhotoBusy = false); return }
        discardPendingPhoto()
        state = state.copy(isPhotoBusy = false,
            photoErrorMessage = message ?: "No se pudo capturar la fotografía. Reintenta o usa la galería.")
    }
    fun discardPendingPhoto() {
        val uri = state.pendingPhotoUri
        state = state.copy(pendingPhotoUri = null, photoErrorMessage = null)
        viewModelScope.launch { photos.discard(uri) }
    }
    fun confirmPendingPhoto() {
        val uri = state.pendingPhotoUri ?: return
        processPhoto { photos.verify(uri); uri }
    }
    fun importPhoto(uri: String) { processPhoto { photos.importPhoto(uri) } }
    private fun processPhoto(operation: suspend () -> String) {
        if (state.isPhotoBusy) return
        state = state.copy(isPhotoBusy = true, photoErrorMessage = null)
        viewModelScope.launch {
            try {
                val path = operation()
                val oldPhoto = state.photoPath
                state = state.copy(photoPath = path, pendingPhotoUri = null, isPhotoBusy = false, photoErrorMessage = null)
                // Las marcas pertenecen a la foto anterior: se descartan, pero el total se conserva como ajuste.
                if (oldPhoto != path && state.markers.isNotEmpty()) {
                    val total = state.musselCount
                    state = state.copy(markers = emptyList(), manualAdjustment = total, countText = total.toString())
                }
                if (oldPhoto != path) photos.discard(oldPhoto)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                state = state.copy(isPhotoBusy = false, photoErrorMessage = e.message ?: "No se pudo guardar la imagen.")
            }
        }
    }
    fun saveSample(onSuccess: (Long) -> Unit) {
        if (state.isSaving || state.isPhotoBusy) return
        state.savedId?.let { onSuccess(it); return }
        if (!validateStep1() || !validateCount() || !validatePhoto()) {
            state = state.copy(errorMessage = "Revisa los datos, el conteo y la fotografía antes de guardar.")
            return
        }
        val snapshot = state
        state = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                photos.verify(checkNotNull(snapshot.photoPath))
                val entity = SampleEntity(code = snapshot.code,
                    concession = snapshot.concession.trim().ifBlank { null }, center = snapshot.centerName.trim(),
                    train = snapshot.trainNumber.trim(), line = snapshot.lineNumber.trim(),
                    timestamp = snapshot.timestamp,
                    sectionLengthCm = checkNotNull(SampleFormat.parseCm(snapshot.sectionLengthCm)),
                    operatorName = snapshot.operatorName.trim(), manualCount = snapshot.musselCount,
                    markerCoordinatesJson = MarkerSerializer.toJson(snapshot.markers),
                    observations = snapshot.observations.trim().ifBlank { "Sin observaciones adicionales." },
                    photoPath = snapshot.photoPath)
                val id = repository.insertSample(entity)
                state = state.copy(isSaving = false, isSaved = true, savedId = id)
                onSuccess(id)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                state = state.copy(isSaving = false, errorMessage = "No se pudo guardar. ${e.message.orEmpty()} Puedes reintentar.")
            }
        }
    }
    fun reset() {
        val old = state
        state = CreateSampleUiState()
        viewModelScope.launch {
            photos.discard(old.pendingPhotoUri)
            // A committed sample owns its image; never delete it on reset.
            if (!old.isSaved) {
                // If the database is temporarily unavailable, retain the file rather than risk deleting evidence.
                val committed = runCatching { repository.findIdByCode(old.code) }.getOrElse { return@launch }
                if (committed == null) photos.discard(old.photoPath)
            }
        }
    }
}
