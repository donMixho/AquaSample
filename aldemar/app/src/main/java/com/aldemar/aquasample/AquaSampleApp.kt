package com.aldemar.aquasample

import android.app.Application
import com.aldemar.aquasample.data.local.AquaSampleDatabase
import com.aldemar.aquasample.data.repository.SampleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase principal de la Aplicación (Application Class).
 *
 * Propósito y funcionamiento:
 * - Se ejecuta antes que cualquier Activity o pantalla.
 * - Sirve como punto de acceso global (Singleton) para instanciar la base de datos Room y el Repositorio.
 * - Al arrancar la app, dispara la corrutina en segundo plano para verificar y precargar
 *   los datos iniciales de prueba (centros de cultivo y muestras de demostración)
 *   para que la app esté lista para ser evaluada o defendida en terreno.
 */
class AquaSampleApp : Application() {

    // Scope global para tareas asíncronas de la app que sobreviven al ciclo de vida de las pantallas
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Instancia única (lazy) de la base de datos Room SQLite
    val database by lazy { AquaSampleDatabase.getDatabase(this, applicationScope) }

    // Repositorio central que conecta los DAOs con la lógica de negocio y los ViewModels
    val repository by lazy { SampleRepository(database.sampleDao()) }

    override fun onCreate() {
        super.onCreate()
        // Asegura que existan centros de cultivo y muestras demo en la BD local
        applicationScope.launch {
            repository.ensureInitialData()
        }
    }
}
