package com.aldemar.aquasample.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withTimeoutOrNull
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import com.aldemar.aquasample.data.model.SampleMarker
import com.aldemar.aquasample.ui.theme.StatusAlert

/**
 * Fotografía ampliable (Telephoto) con una capa de marcas de conteo.
 *
 * Sincronización: Telephoto expone `transformedContentBounds`, el rectángulo que la imagen ocupa
 * hoy en pantalla (con zoom y desplazamiento ya aplicados). Tanto el dibujo de las marcas como la
 * conversión de un toque a coordenadas se calculan contra ese rectángulo, de modo que las marcas
 * (normalizadas 0..1 sobre la imagen original) quedan fijas sobre la foto en cualquier zoom.
 *
 * Gestos: pellizcar para ampliar, arrastrar para mover y tocar para marcar. El doble toque no hace
 * zoom (se contaría como dos marcas rápidas), y los toques se detectan sin consumirlos ni esperar
 * el tiempo de doble toque.
 */
@Composable
fun MarkedPhotoViewer(
    photo: Any,
    markers: List<SampleMarker>,
    onAddMarker: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomableState = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 8f))
    val imageState = rememberZoomableImageState(zoomableState)
    val currentAdd by rememberUpdatedState(onAddMarker)

    Box(
        modifier = modifier
            .clipToBounds()
            .background(Color.Black)
            .observeTaps { tap ->
                val bounds = zoomableState.transformedContentBounds
                if (bounds.width > 0f && bounds.height > 0f && bounds.contains(tap)) {
                    currentAdd((tap.x - bounds.left) / bounds.width, (tap.y - bounds.top) / bounds.height)
                }
            }
    ) {
        ZoomableAsyncImage(
            model = photo,
            contentDescription = "Fotografía de la muestra para conteo",
            state = imageState,
            modifier = Modifier.fillMaxSize(),
            onDoubleClick = DoubleClickToZoomListener { _, _ -> }
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bounds = zoomableState.transformedContentBounds
            if (bounds.width <= 0f || bounds.height <= 0f) return@Canvas
            val radius = 7.dp.toPx()
            val ring = 2.dp.toPx()
            markers.forEach { marker ->
                val center = Offset(bounds.left + marker.x * bounds.width, bounds.top + marker.y * bounds.height)
                drawCircle(Color.White, radius = radius + ring, center = center)
                drawCircle(StatusAlert, radius = radius, center = center)
            }
        }
    }
}

/** Detecta toques simples en el pase Initial sin consumirlos: el zoom y el desplazamiento siguen funcionando. */
@Composable
private fun Modifier.observeTaps(onTap: (Offset) -> Unit): Modifier {
    val currentOnTap by rememberUpdatedState(onTap)
    val slop = LocalViewConfiguration.current.touchSlop
    val longPress = LocalViewConfiguration.current.longPressTimeoutMillis
    return pointerInput(slop, longPress) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var tap: Offset? = null
            withTimeoutOrNull(longPress) {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.changes.size > 1) break // varios dedos: es un pellizco, no un toque
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if ((change.position - down.position).getDistance() > slop) break // arrastre
                    if (!change.pressed) { tap = change.position; break }
                }
            }
            tap?.let(currentOnTap)
        }
    }
}
