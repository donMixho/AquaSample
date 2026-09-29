package com.aldemar.aquasample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estado inmutable que encapsula los datos capturados durante los 4 pasos del wizard.
 */
data class CreateSampleUiState(
    val centerName: String = "Centro Huar",
    val trainNumber: String = "Tren 01",
    val lineNumber: String = "Línea 01",
    val sampleDate: String = "",
    val sectionLengthMeters: String = "1.0",
    val operatorName: String = "Carlos Delgado",
    val photoPath: String? = null,
    val musselCount: Int = 0,
    val observations: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedId: Long? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel que orquesta el flujo de creación de una nueva muestra (Wizard de 4 pasos).
 *
 * Responsabilidades:
 * - Almacenar los datos temporales ingresados por el operador a través de las 4 pantallas:
 *   (1. Ubicación ➔ 2. Foto ➔ 3. Conteo ➔ 4. Resumen).
 * - Proporcionar métodos ergonómicos para el conteo rápido (+1, +10, +50, -1, -10).
 * - Validar que los campos obligatorios estén presentes antes de persistir.
 * - Crear y persistir la entidad 'SampleEntity' en la base de datos Room SQLite.
 */
class SampleCreateViewModel(
    private val repository: SampleRepository
) : ViewModel() {

    // Lista de centros de cultivo disponibles en la base de datos
    val centers: StateFlow<List<CenterEntity>> = repository.allCenters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(CreateSampleUiState())
    val uiState: StateFlow<CreateSampleUiState> = _uiState.asStateFlow()

    init {
        resetDateTime()
    }

    /**
     * Establece la fecha y hora actual automáticamente.
     */
    fun resetDateTime() {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        _uiState.value = _uiState.value.copy(sampleDate = formatter.format(Date()))
    }

    fun setOperator(name: String) {
        _uiState.value = _uiState.value.copy(operatorName = name)
    }

    fun updateCenter(center: String) {
        _uiState.value = _uiState.value.copy(centerName = center)
    }

    fun updateTrain(train: String) {
        _uiState.value = _uiState.value.copy(trainNumber = train)
    }

    fun updateLine(line: String) {
        _uiState.value = _uiState.value.copy(lineNumber = line)
    }

    fun updateSectionLength(length: String) {
        _uiState.value = _uiState.value.copy(sectionLengthMeters = length)
    }

    fun updatePhotoPath(path: String?) {
        _uiState.value = _uiState.value.copy(photoPath = path)
    }

    /**
     * Incrementa o decrementa el conteo de choritos evitando valores negativos.
     */
    fun incrementCount(amount: Int) {
        val current = _uiState.value.musselCount
        val next = (current + amount).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(musselCount = next)
    }

    /**
     * Permite fijar directamente un número escrito a mano por el operador.
     */
    fun setCount(count: Int) {
        _uiState.value = _uiState.value.copy(musselCount = count.coerceAtLeast(0))
    }

    fun updateObservations(notes: String) {
        _uiState.value = _uiState.value.copy(observations = notes)
    }

    /**
     * Validación de negocio: exige centro, tren, línea y operador obligatorios.
     */
    fun validateStep1(): Boolean {
        val state = _uiState.value
        return state.centerName.isNotBlank() &&
                state.trainNumber.isNotBlank() &&
                state.lineNumber.isNotBlank() &&
                state.operatorName.isNotBlank()
    }

    /**
     * Guarda la muestra en la base de datos local SQLite de Room.
     */
    fun saveSample(onSuccess: (Long) -> Unit) {
        val state = _uiState.value
        val length = state.sectionLengthMeters.toDoubleOrNull() ?: 1.0

        _uiState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Genera un código correlativo legible (ej: MUE-8492)
                val code = "MUE-${System.currentTimeMillis().toString().takeLast(4)}"
                val entity = SampleEntity(
                    code = code,
                    centerName = state.centerName,
                    trainNumber = state.trainNumber,
                    lineNumber = state.lineNumber,
                    sampleDate = state.sampleDate.ifBlank {
                        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                    },
                    sectionLengthMeters = length,
                    operatorName = state.operatorName,
                    musselCount = state.musselCount,
                    observations = state.observations.ifBlank { "Sin observaciones adicionales." },
                    photoPath = state.photoPath,
                    status = "PENDIENTE",
                    supervisorComment = null,
                    supervisorName = null
                )

                // Inserción en Room a través del repositorio
                val id = repository.insertSample(entity)
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true, savedId = id)
                onSuccess(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }

    /**
     * Reinicia el estado del formulario para el siguiente muestreo.
     */
    fun reset() {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        _uiState.value = CreateSampleUiState(sampleDate = formatter.format(Date()))
    }
}
