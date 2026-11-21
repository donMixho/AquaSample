package com.aldemar.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.SyncStatus

/**
 * Tabla 'samples'. Jerarquía: Concesión -> Centro -> Tren -> Línea -> Muestra.
 * Funciona 100% offline; [syncStatus] indica si el registro ya llegó al backend.
 */
@Entity(
    tableName = "samples",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["syncStatus"])
    ]
)
data class SampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Código único visible de la muestra (ej: MUE-2026-001). */
    val code: String,

    val concession: String? = null,
    val center: String,
    val train: String,
    val line: String,

    /** Longitud del tramo en centímetros. */
    val sectionLengthCm: Int,

    /** Momento del muestreo, epoch ms. */
    val timestamp: Long,

    val operatorName: String,

    /** Conteo final (ajustable manualmente). */
    val manualCount: Int,

    /** JSON con las marcas sobre la foto: [{"x":0.12,"y":0.55}, ...] con x,y normalizados 0..1. */
    val markerCoordinatesJson: String = "[]",

    val observations: String,
    val photoPath: String? = null,

    val reviewStatus: SampleStatus = SampleStatus.PENDIENTE_REVISION,
    val syncStatus: SyncStatus = SyncStatus.PENDIENTE_SINCRONIZACION,

    val supervisorComment: String? = null,
    val supervisorName: String? = null
)
