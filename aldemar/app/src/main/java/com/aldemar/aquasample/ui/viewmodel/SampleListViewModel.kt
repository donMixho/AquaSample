package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel que alimenta la pantalla principal e historial de muestras (HomeScreen).
 *
 * Principio reactivo con StateFlow:
 * - Utiliza el operador 'combine' de Kotlin Coroutines para combinar 4 flujos en tiempo real:
 *   1. El flujo de muestras de la base de datos (Room).
 *   2. El texto escrito en la barra de búsqueda.
 *   3. El chip de estado seleccionado (Todos, Pendientes, Validados, Observados).
 *   4. El filtro de centro si aplica.
 * - Cualquier cambio en la BD o en los filtros recalcula instantáneamente la lista resultante.
 */
class SampleListViewModel(
    private val repository: SampleRepository
) : ViewModel() {

    val centers: StateFlow<List<CenterEntity>> = repository.allCenters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Texto del buscador en tiempo real
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtro por estado (null = todos)
    private val _selectedStatusFilter = MutableStateFlow<SampleStatus?>(null)
    val selectedStatusFilter: StateFlow<SampleStatus?> = _selectedStatusFilter.asStateFlow()

    // Filtro por centro (null = todos)
    private val _selectedCenterFilter = MutableStateFlow<String?>(null)
    val selectedCenterFilter: StateFlow<String?> = _selectedCenterFilter.asStateFlow()

    /**
     * Lista filtrada expuesta como StateFlow reactivo para la UI de Compose.
     */
    val filteredSamples: StateFlow<List<SampleEntity>> = combine(
        repository.allSamples,
        _searchQuery,
        _selectedStatusFilter,
        _selectedCenterFilter
    ) { samples, query, statusFilter, centerFilter ->
        samples.filter { sample ->
            val matchesQuery = query.isBlank() ||
                    sample.code.contains(query, ignoreCase = true) ||
                    sample.centerName.contains(query, ignoreCase = true) ||
                    sample.trainNumber.contains(query, ignoreCase = true) ||
                    sample.lineNumber.contains(query, ignoreCase = true) ||
                    sample.operatorName.contains(query, ignoreCase = true)

            val matchesStatus = statusFilter == null || sample.status.equals(statusFilter.name, ignoreCase = true)

            val matchesCenter = centerFilter == null || sample.centerName.equals(centerFilter, ignoreCase = true)

            matchesQuery && matchesStatus && matchesCenter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: SampleStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setCenterFilter(center: String?) {
        _selectedCenterFilter.value = center
    }
}
