package com.aldemar.aquasample.data.model

/** Estado de SINCRONIZACIÓN con el backend (modo offline / WorkManager). */
enum class SyncStatus(val label: String) {
    SINCRONIZADO("Sincronizado"),
    PENDIENTE_SINCRONIZACION("Pendiente de sincronización"),
    ERROR_SINCRONIZACION("Error de sincronización")
}
