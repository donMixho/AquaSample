package com.aldemar.aquasample.ui.screens.sample

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.theme.AquaBackground
import com.aldemar.aquasample.ui.theme.AquaOutline
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSecondary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextPrimary
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.theme.StatusAlert
import com.aldemar.aquasample.ui.theme.StatusValidated
import com.aldemar.aquasample.ui.viewmodel.SampleCreateViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoCaptureScreen(
    viewModel: SampleCreateViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCount: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { successful ->
        val pendingUri = uiState.pendingPhotoUri?.let(Uri::parse)
        if (!successful) {
            pendingUri?.let { deleteUri(context, it) }
            viewModel.discardPendingPhoto()
        }
    }

    val launchCamera = {
        var temporaryUri: Uri? = null
        try {
            val imageDirectory = File(context.filesDir, "images")
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                error("No se pudo crear el directorio de imágenes.")
            }
            val temporaryFile = File.createTempFile("sample_photo_", ".jpg", imageDirectory)
            temporaryUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                temporaryFile
            )
            viewModel.setPendingPhotoUri(temporaryUri.toString())
            cameraLauncher.launch(temporaryUri)
        } catch (exception: Exception) {
            temporaryUri?.let { deleteUri(context, it) }
            viewModel.discardPendingPhoto()
            viewModel.setPhotoError(
                exception.message ?: "No se pudo iniciar la cámara."
            )
        }
    }

    val isPreview = uiState.pendingPhotoUri != null
    val displayedPhoto = uiState.pendingPhotoUri ?: uiState.photoPath

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Evidencia Fotográfica (Paso 2/4)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { 0.50f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AquaSecondary,
                trackColor = AquaOutline.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AquaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPreview) "Vista previa de la fotografía" else "Registro Visual de la Muestra",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (isPreview) {
                            "Confirme la imagen para adjuntarla a esta muestra o vuelva a tomarla."
                        } else {
                            "Tome una fotografía de la cuerda de choritos extendida sobre la mesa de muestreo."
                        },
                        fontSize = 12.sp,
                        color = AquaTextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AquaBackground)
                            .border(1.dp, AquaOutline, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (displayedPhoto != null) {
                            AsyncImage(
                                model = displayedPhoto,
                                contentDescription = "Foto de muestra",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = AquaTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sin fotografía capturada",
                                    fontSize = 13.sp,
                                    color = AquaTextSecondary
                                )
                                Text(
                                    text = "Presione el botón para abrir la cámara",
                                    fontSize = 11.sp,
                                    color = AquaTextSecondary
                                )
                            }
                        }
                    }

                    if (uiState.photoErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.photoErrorMessage.orEmpty(),
                            color = StatusAlert,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isPreview) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    confirmCapturedPhoto(context, viewModel, uiState.pendingPhotoUri)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Confirmar foto", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    uiState.pendingPhotoUri?.let { deleteUri(context, Uri.parse(it)) }
                                    viewModel.discardPendingPhoto()
                                    launchCamera()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AquaPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Volver a tomar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Button(
                            onClick = launchCamera,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (uiState.photoPath == null) "Tomar foto" else "Cambiar foto",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (uiState.photoPath != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusValidated,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = "Evidencia vinculada correctamente",
                                    fontSize = 12.sp,
                                    color = StatusValidated,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            if (!isPreview) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                ) {
                    Text(
                        text = "Siguiente: Conteo y Observaciones ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AquaSurface
                    )
                }
            }
        }
    }
}

private fun confirmCapturedPhoto(
    context: Context,
    viewModel: SampleCreateViewModel,
    pendingPhotoUri: String?
) {
    if (pendingPhotoUri == null) {
        viewModel.setPhotoError("No hay una foto pendiente para confirmar.")
        return
    }

    var destinationFile: File? = null
    try {
        val imageDirectory = File(context.filesDir, "images")
        val timestamp = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(Date())
        var suffix = 0
        var candidate: File
        do {
            val fileName = if (suffix == 0) "$timestamp.jpg" else "${timestamp}_$suffix.jpg"
            candidate = File(imageDirectory, fileName)
            suffix++
        } while (candidate.exists())
        destinationFile = candidate

        val sourceUri = Uri.parse(pendingPhotoUri)
        val input = context.contentResolver.openInputStream(sourceUri)
            ?: error("No se pudo leer la fotografía capturada.")
        input.use { source ->
            destinationFile.outputStream().use { target -> source.copyTo(target) }
        }
        if (destinationFile.length() == 0L) {
            error("La fotografía capturada está vacía.")
        }

        val finalUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destinationFile
        )
        deleteUri(context, sourceUri)
        viewModel.confirmPhoto(finalUri.toString())
    } catch (exception: Exception) {
        destinationFile?.delete()
        viewModel.setPhotoError(
            exception.message ?: "No se pudo guardar la fotografía."
        )
    }
}

private fun deleteUri(context: Context, uri: Uri) {
    context.contentResolver.delete(uri, null, null)
}
