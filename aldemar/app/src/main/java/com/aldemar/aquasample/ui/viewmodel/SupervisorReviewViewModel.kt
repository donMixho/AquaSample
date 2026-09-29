package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado inmutable de la pantalla de revisión del supervisor.
 */
data class ReviewUiState(
    val sample: SampleEntity? = null,
    val supervisorComment: String = "",
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para el módulo de control de calidad y revisión técnica (Supervisor).
 *
 * Funcionalidad:
 * - Carga la muestra seleccionada desde SQLite.
 * - Permite redactar un comentario o instrucción técnica.
 * - Actualiza el estado a VALIDADO (verde) u OBSERVADO (naranja) y guarda el nombre del supervisor.
 */
class SupervisorReviewViewModel(
    private val repository: SampleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    /**
     * Carga de forma asíncrona la muestra por ID.
     */
    fun loadSample(sampleId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.getSampleById(sampleId).collect { entity ->
                _uiState.value = _uiState.value.copy(
                    sample = entity,
                    supervisorComment = entity?.supervisorComment ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(supervisorComment = comment)
    }

    /**
     * Aplica el dictamen técnico en la base de datos Room.
     */
    fun applyReview(
        newStatus: SampleStatus,
        supervisorName: String,
        onSuccess: () -> Unit
    ) {
        val currentSample = _uiState.value.sample ?: return
        val comment = _uiState.value.supervisorComment.ifBlank {
            if (newStatus == SampleStatus.VALIDADO) "Muestra revisada y validada conforme a pauta técnica."
            else "Muestra observada para rectificación técnica en terreno."
        }

        viewModelScope.launch {
            try {
                repository.updateStatus(
                    id = currentSample.id,
                    status = newStatus.name,
                    comment = comment,
                    supervisorName = supervisorName
                )
                _uiState.value = _uiState.value.copy(isUpdated = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
