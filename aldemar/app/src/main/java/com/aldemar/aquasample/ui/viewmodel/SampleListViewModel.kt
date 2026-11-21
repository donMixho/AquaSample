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
    val selectedTrain = savedState.getStateFlow<String?>("train", null)
    val selectedLine = savedState.getStateFlow<String?>("line", null)
    val startDate = savedState.getStateFlow<Long?>("start", null)
    val endDate = savedState.getStateFlow<Long?>("end", null)
    val centers = repository.allCenters.catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    /** Opciones de los filtros de tren y línea, tomadas de las muestras existentes. */
    val trains = repository.allSamples.map { l -> l.map { it.train.trim() }.distinctBy { it.lowercase() }.sortedBy { it.lowercase() } }
        .catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lines = repository.allSamples.map { l -> l.map { it.line.trim() }.distinctBy { it.lowercase() }.sortedBy { it.lowercase() } }
        .catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val retry = MutableStateFlow(0)

    private val filters = combine(searchQuery, statusName, selectedCenterFilter, selectedTrain, selectedLine) { q, st, c, t, l ->
        HistoryFilter(q, st?.let { SampleStatus.fromString(it) }, c, t, l)
    }.combine(startDate) { f, s -> f.copy(startDate = s) }.combine(endDate) { f, e -> f.copy(endDate = e) }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState = retry.flatMapLatest {
        combine(repository.allSamples, filters) { samples, f -> SampleListUiState(SampleFilter.apply(samples, f), false) }
            .onStart { emit(SampleListUiState()) }
            .catch { emit(SampleListUiState(isLoading = false, error = "No se pudo cargar el historial. Reintenta.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleListUiState())
    fun updateSearchQuery(value: String) { savedState["query"] = value.take(100) }
    fun setStatusFilter(value: SampleStatus?) { savedState["status"] = value?.name }
    fun setCenterFilter(value: String?) { savedState["center"] = value }
    fun setTrainFilter(value: String?) { savedState["train"] = value }
    fun setLineFilter(value: String?) { savedState["line"] = value }
    fun setStartDate(value: Long?) { savedState["start"] = value }
    fun setEndDate(value: Long?) { savedState["end"] = value }
    fun clearFilters() {
        updateSearchQuery(""); setStatusFilter(null); setCenterFilter(null)
        setTrainFilter(null); setLineFilter(null); setStartDate(null); setEndDate(null)
    }
    fun retryLoad() { retry.value += 1 }
}
