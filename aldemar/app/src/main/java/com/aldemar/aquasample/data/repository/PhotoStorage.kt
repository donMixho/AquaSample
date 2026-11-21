package com.aldemar.aquasample.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Owns image files; all disk operations run off the main thread. */
class PhotoStorage(context: Context) {
    private val app = context.applicationContext
    private val authority = "${app.packageName}.fileprovider"
    private val directory get() = File(app.filesDir, "images").apply {
        check(exists() || mkdirs()) { "No se pudo crear la carpeta de fotografías." }
    }
    /** Destino de una captura de CameraX: el archivo donde se escribe y su URI (FileProvider) para Coil. */
    class CaptureTarget(val file: File, val uri: String)

    /**
     * Crea (vacío) el archivo de la foto dentro de filesDir/images, con el código único de la muestra
     * en el nombre. Una nueva toma de la misma muestra genera otro archivo; el anterior se descarta.
     */
    suspend fun prepareCapture(sampleCode: String): CaptureTarget = withContext(Dispatchers.IO) {
        val safeCode = sampleCode.replace(Regex("[^A-Za-z0-9_-]"), "_").take(64).ifBlank { "muestra" }
        val file = File(directory, "${safeCode}_${System.currentTimeMillis()}.jpg")
        check(file.exists() || file.createNewFile()) { "No se pudo crear el archivo de la fotografía." }
        CaptureTarget(file, FileProvider.getUriForFile(app, authority, file).toString())
    }
    suspend fun verify(uri: String) = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        app.contentResolver.openInputStream(Uri.parse(uri)).use {
            checkNotNull(it) { "No se pudo abrir la fotografía." }
            BitmapFactory.decodeStream(it, null, options)
        }
        check(options.outWidth > 0 && options.outHeight > 0) { "El archivo no contiene una imagen válida." }
    }
    suspend fun importPhoto(uri: String): String = withContext(Dispatchers.IO) {
        val file = File.createTempFile("gallery_", ".jpg", directory)
        try {
            app.contentResolver.openInputStream(Uri.parse(uri)).use { input ->
                checkNotNull(input) { "No se pudo abrir la imagen seleccionada." }
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        check(total <= 25L * 1024 * 1024) { "Selecciona una imagen de hasta 25 MB." }
                        output.write(buffer, 0, read)
                    }
                }
            }
            val stored = FileProvider.getUriForFile(app, authority, file).toString()
            verify(stored)
            stored
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }
    suspend fun discard(uri: String?) = withContext(Dispatchers.IO) {
        if (uri != null && Uri.parse(uri).authority == authority) {
            runCatching { app.contentResolver.delete(Uri.parse(uri), null, null) }
        }
        Unit
    }
}
