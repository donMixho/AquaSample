package com.aldemar.aquasample

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquasample.data.repository.PhotoStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoStorageTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    @Test fun validCaptureCanBeImportedAndRemainsReadable() = runBlocking {
        val storage = PhotoStorage(context)
        val target = storage.prepareCapture("MUE-TEST-1")
        val source = target.uri
        var imported: String? = null
        try {
            context.contentResolver.openOutputStream(Uri.parse(source))!!.use {
                val bitmap=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888)
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,it)
                bitmap.recycle()
            }
            storage.verify(source)
            imported=storage.importPhoto(source)
            assertNotEquals(source,imported)
            storage.discard(source)
            PhotoStorage(context).verify(imported!!)
        } finally { storage.discard(source); storage.discard(imported) }
    }
    @Test fun captureFileLivesInPrivateImagesFolderAndNameCarriesSampleCode() = runBlocking {
        val storage = PhotoStorage(context)
        val target = storage.prepareCapture("MUE-2026/ab c")
        try {
            assertTrue(target.file.exists())
            assertEquals(java.io.File(context.filesDir, "images").canonicalPath, target.file.parentFile!!.canonicalPath)
            assertTrue(target.file.name.startsWith("MUE-2026_ab_c_"))
            assertTrue(target.file.name.endsWith(".jpg"))
            storage.discard(target.uri)
            assertFalse(target.file.exists())
        } finally { storage.discard(target.uri) }
    }
    @Test fun emptyOrInvalidCaptureIsRejected() = runBlocking {
        val storage = PhotoStorage(context)
        val target = storage.prepareCapture("MUE-TEST-1")
        val source = target.uri
        try {
            var rejected=false
            try { storage.verify(source) } catch (_: IllegalStateException) { rejected=true }
            assertTrue(rejected)
        } finally { storage.discard(source) }
    }
}
