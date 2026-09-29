package com.aldemar.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de persistencia Room: Tabla 'centers'.
 *
 * Almacena el catálogo de centros de cultivo de choritos operados por ALDEMAR SpA
 * en la Región de Los Lagos (Calbuco, Huar, Chidhuapi, Quellón).
 *
 * Facilita que en el formulario el operador seleccione un centro ya normalizado
 * en lugar de escribirlo a mano y cometer faltas de ortografía o duplicidad.
 */
@Entity(tableName = "centers")
data class CenterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,       // Nombre del centro (ej: Centro Huar)
    val location: String    // Zona o fiordo (ej: Isla Huar - Calbuco)
)
