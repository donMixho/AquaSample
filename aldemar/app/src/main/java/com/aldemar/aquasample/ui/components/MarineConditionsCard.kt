package com.aldemar.aquasample.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquasample.AquaSampleApp
import com.aldemar.aquasample.data.remote.marine.MarineConditions
import com.aldemar.aquasample.data.remote.marine.MarineState
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.theme.AquaSurface
import com.aldemar.aquasample.ui.theme.AquaTextSecondary
import com.aldemar.aquasample.ui.viewmodel.SampleFormat

private fun fmt(v: Double?, unit: String) = v?.let { "%.1f %s".format(java.util.Locale.US, it, unit) } ?: "s/d"

/** Tarjeta compacta de condiciones marinas (Open-Meteo Marine) con caché offline. */
@Composable
fun MarineConditionsCard(modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as AquaSampleApp
    var state by remember { mutableStateOf<MarineState?>(null) }
    LaunchedEffect(Unit) { state = app.marineRepository.load() }
    MarineConditionsContent(state, modifier)
}

@Composable
fun MarineConditionsContent(state: MarineState?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AquaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Condiciones Marinas Referenciales", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AquaPrimary)
            val c: MarineConditions? = state?.conditions
            when {
                state == null -> Text("Consultando…", fontSize = 12.sp, color = AquaTextSecondary)
                c == null -> Text("Sin conexión y sin datos guardados.", fontSize = 12.sp, color = AquaTextSecondary)
                else -> {
                    Text("📍 ${c.locationName} (${c.latitude}, ${c.longitude})", fontSize = 12.sp, color = AquaTextSecondary)
                    Text(
                        "🌊 Ola ${fmt(c.waveHeightM, "m")} · Período ${fmt(c.wavePeriodS, "s")} · Dir. ${fmt(c.waveDirectionDeg, "°")}",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    Text("🌡 Temp. superficial del mar ${fmt(c.seaTempC, "°C")}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Consulta: ${SampleFormat.dateTime(c.fetchedAt)}" + if (state.stale) " (sin conexión, último dato guardado)" else "",
                        fontSize = 11.sp, color = AquaTextSecondary
                    )
                }
            }
            Text(
                "Datos referenciales estimados (Open-Meteo Marine), no medición directa del centro",
                fontSize = 11.sp, color = AquaTextSecondary, fontWeight = FontWeight.Light
            )
        }
    }
}
