package com.aldemar.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad principal de persistencia Room: Tabla 'samples'.
 *
 * Trazabilidad jerárquica exigida por el caso de negocio:
 * Centro de Cultivo ➔ Tren/Módulo ➔ Línea/Cuerda ➔ Muestra.
 *
 * Esta tabla permite que el operador trabaje 100% desconectado en el mar (Offline-First).
 * Nada depende de APIs remotas ni conexión 4G/WiFi en terreno.
 */
@Entity(tableName = "samples")
data class SampleEntity(
    // Identificador autonumérico único de la base de datos
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Código visible de trazabilidad de la muestra (ej: MUE-2026-001)
    val code: String,

    // Datos de ubicación marítima
    val centerName: String,         // Nombre del centro (ej: Centro Huar)
    val trainNumber: String,        // Tren o módulo de cultivo (ej: Tren 01)
    val lineNumber: String,         // Cuerda o línea muestreada (ej: Línea 14)

    // Datos de la toma de muestra
    val sampleDate: String,         // Fecha y hora del muestreo en terreno
    val sectionLengthMeters: Double,// Longitud del tramo cortado/medido (ej: 1.0 m)
    val operatorName: String,       // Nombre del operador responsable
    val musselCount: Int,           // Cantidad total de individuos (choritos)
    val observations: String,       // Notas técnicas (calibre, desprendimiento, algas, depredadores)

    // Evidencia visual asociada
    val photoPath: String? = null,  // URI o ruta local interna de la fotografía

    // Flujo de supervisión y control de calidad
    val status: String = "PENDIENTE",       // PENDIENTE, VALIDADO, OBSERVADO
    val supervisorComment: String? = null,  // Comentario técnico emitido por el supervisor
    val supervisorName: String? = null,     // Nombre del supervisor que revisó
    val createdAt: Long = System.currentTimeMillis() // Timestamp para orden cronológico
)
