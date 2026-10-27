package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(val sample: SampleEntity? = null, val supervisorComment: String = "",
    val isLoading: Boolean = true, val isSaving: Boolean = false, val isUpdated: Boolean = false,
    val errorMessage: String? = null)

class SupervisorReviewViewModel(private val repository: SampleRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var loadedId: Long? = null
    private var commentEdited = false
    fun loadSample(sampleId: Long) {
        if (loadedId == sampleId && loadJob?.isActive == true) return
        loadJob?.cancel()
        loadedId = sampleId
        commentEdited = false
        _uiState.value = ReviewUiState()
        loadJob = viewModelScope.launch {
            try {
                repository.getSampleById(sampleId).collect { sample ->
                    _uiState.value = _uiState.value.copy(sample = sample, isLoading = false,
                        supervisorComment = if (commentEdited) _uiState.value.supervisorComment else sample?.supervisorComment.orEmpty(),
                        errorMessage = if (sample == null) "La muestra no existe." else null)
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No se pudo cargar la muestra.")
            }
        }
    }
    fun updateComment(comment: String) {
        commentEdited = true
        _uiState.value = _uiState.value.copy(supervisorComment = comment.take(2001), errorMessage = null)
    }
    fun applyReview(newStatus: SampleStatus, supervisorName: String, role: UserRole, onSuccess: () -> Unit) {
        if (_uiState.value.isSaving) return
        val sample = _uiState.value.sample ?: return
        val comment = _uiState.value.supervisorComment.trim()
        val error = when {
            role != UserRole.SUPERVISOR -> "Solo el perfil supervisor puede emitir un dictamen."
            supervisorName.isBlank() -> "Falta el nombre del supervisor."
            newStatus == SampleStatus.PENDIENTE -> "Selecciona Validar u Observar."
            else -> SampleValidation.validateReview(newStatus == SampleStatus.OBSERVADO, comment)
        }
        if (error != null) { _uiState.value = _uiState.value.copy(errorMessage = error); return }
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                repository.updateStatus(sample.id, newStatus.name,
                    comment.ifBlank { "Muestra revisada y validada." }, supervisorName.trim())
                _uiState.value = _uiState.value.copy(isSaving = false, isUpdated = true)
                onSuccess()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "No se pudo guardar el dictamen. Reintenta.")
            }
        }
    }
}
