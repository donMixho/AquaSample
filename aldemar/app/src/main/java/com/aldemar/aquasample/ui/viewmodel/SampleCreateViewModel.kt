package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.repository.PhotoStorage
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CreateSampleUiState(
    val centerName: String = "Centro Huar",
    val trainNumber: String = "",
    val lineNumber: String = "",
    val sampleDate: String = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date()),
    val sectionLengthMeters: String = "1.0",
    val operatorName: String = "",
    val pendingPhotoUri: String? = null,
    val photoPath: String? = null,
    val photoErrorMessage: String? = null,
    val isPhotoBusy: Boolean = false,
    val countText: String = "0",
    val observations: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedId: Long? = null,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val code: String = "MUE-${UUID.randomUUID()}"
) : Serializable {
    val musselCount: Int get() = countText.trim().toIntOrNull() ?: 0
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
    fun updateSectionLength(value: String) { state = state.copy(sectionLengthMeters = value.take(32), fieldErrors = state.fieldErrors - "length") }
    fun updateObservations(value: String) { state = state.copy(observations = value.take(2000)) }
    fun updateCount(value: String) {
        val text = value.take(16)
        val error = SampleValidation.validateCount(text)
        state = state.copy(countText = text, fieldErrors = (state.fieldErrors - "count") +
            if (error == null) emptyMap() else mapOf("count" to error))
    }
    fun incrementCount(amount: Int) {
        val next = (state.musselCount.toLong() + amount).coerceIn(0, SampleValidation.MAX_COUNT.toLong())
        updateCount(next.toString())
    }
    fun validateStep1(): Boolean {
        val errors = SampleValidation.validateLocation(state.centerName, state.trainNumber,
            state.lineNumber, state.operatorName, state.sectionLengthMeters)
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
    fun prepareCamera(onReady: (String) -> Unit) {
        if (state.isPhotoBusy) return
        state = state.copy(isPhotoBusy = true, photoErrorMessage = null)
        viewModelScope.launch {
            try {
                photos.discard(state.pendingPhotoUri)
                val uri = photos.prepareCamera()
                state = state.copy(pendingPhotoUri = uri, isPhotoBusy = false)
                onReady(uri)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                photos.discard(state.pendingPhotoUri)
                state = state.copy(pendingPhotoUri = null, isPhotoBusy = false,
                    photoErrorMessage = "No se pudo abrir la cámara. Puedes usar la galería.")
            }
        }
    }
    fun cameraResult(success: Boolean) {
        if (!success) discardPendingPhoto()
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
                val entity = SampleEntity(code = snapshot.code, centerName = snapshot.centerName.trim(),
                    trainNumber = snapshot.trainNumber.trim(), lineNumber = snapshot.lineNumber.trim(),
                    sampleDate = snapshot.sampleDate,
                    sectionLengthMeters = snapshot.sectionLengthMeters.trim().replace(',', '.').toDouble(),
                    operatorName = snapshot.operatorName.trim(), musselCount = snapshot.musselCount,
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
