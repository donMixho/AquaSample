package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/** Todas las muestras para Dashboard y comparación de períodos (el cálculo vive en objetos puros). */
class AnalyticsViewModel(repository: SampleRepository) : ViewModel() {
    val samples = repository.allSamples.catch { emit(emptyList<SampleEntity>()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
