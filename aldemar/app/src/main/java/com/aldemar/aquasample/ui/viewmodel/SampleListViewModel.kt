package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.flow.*

data class SampleListUiState(val samples: List<SampleEntity> = emptyList(),
    val isLoading: Boolean = true, val error: String? = null)

class SampleListViewModel(repository: SampleRepository, private val savedState: SavedStateHandle) : ViewModel() {
    val message = savedState.getStateFlow<String?>("message", null)
    fun showMessage(value: String) { savedState["message"] = value }
    fun consumeMessage() { savedState["message"] = null }
    val searchQuery = savedState.getStateFlow("query", "")
    private val statusName = savedState.getStateFlow<String?>("status", null)
    val selectedStatusFilter = statusName.map { name -> name?.let { SampleStatus.fromString(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), statusName.value?.let { SampleStatus.fromString(it) })
    val selectedCenterFilter = savedState.getStateFlow<String?>("center", null)
    val centers = repository.allCenters.catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val retry = MutableStateFlow(0)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState = retry.flatMapLatest {
        combine(repository.allSamples, searchQuery, statusName, selectedCenterFilter) { samples, query, status, center ->
            SampleListUiState(SampleFilter.apply(samples, query, status?.let { SampleStatus.fromString(it) }, center), false)
        }.onStart { emit(SampleListUiState()) }
            .catch { emit(SampleListUiState(isLoading = false, error = "No se pudo cargar el historial. Reintenta.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleListUiState())
    fun updateSearchQuery(value: String) { savedState["query"] = value.take(100) }
    fun setStatusFilter(value: SampleStatus?) { savedState["status"] = value?.name }
    fun setCenterFilter(value: String?) { savedState["center"] = value }
    fun clearFilters() { updateSearchQuery(""); setStatusFilter(null); setCenterFilter(null) }
    fun retryLoad() { retry.value += 1 }
}
