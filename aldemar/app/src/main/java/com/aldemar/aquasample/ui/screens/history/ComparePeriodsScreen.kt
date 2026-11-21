package com.aldemar.aquasample.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.components.DateField
import com.aldemar.aquasample.ui.components.FilterDropdown
import com.aldemar.aquasample.ui.theme.*
import com.aldemar.aquasample.ui.viewmodel.AnalyticsViewModel
import com.aldemar.aquasample.ui.viewmodel.HistoryAnalytics
import java.util.Locale

/** Compara dos rangos de fechas de una misma línea (individuos y densidad ind/cm). */
@Composable
fun ComparePeriodsScreen(viewModel: AnalyticsViewModel, onNavigateBack: () -> Unit) {
    val samples by viewModel.samples.collectAsStateWithLifecycle()
    val lines = remember(samples) { HistoryAnalytics.lines(samples) }
    var label by rememberSaveable { mutableStateOf<String?>(null) }
    var aStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var aEnd by rememberSaveable { mutableStateOf<Long?>(null) }
    var bStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var bEnd by rememberSaveable { mutableStateOf<Long?>(null) }
    val line = lines.firstOrNull { it.label == label } ?: lines.firstOrNull()
    val lineSamples = remember(samples, line) { line?.let { HistoryAnalytics.forLine(samples, it) }.orEmpty() }
    val rangesSet = (aStart != null || aEnd != null) && (bStart != null || bEnd != null)
    val a = HistoryAnalytics.inRange(lineSamples, aStart, aEnd)
    val b = HistoryAnalytics.inRange(lineSamples, bStart, bEnd)
    val result = if (rangesSet) HistoryAnalytics.compare(a, b) else null

    Scaffold(
        topBar = { AquaTopBar("Comparar períodos", canNavigateBack = true, onNavigateBack = onNavigateBack) },
        containerColor = AquaBackground
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lines.isEmpty()) { Text("Aún no hay muestras para comparar.", color = AquaTextSecondary); return@Column }
            Text("Línea (centro · tren · línea)", fontWeight = FontWeight.Bold, color = AquaPrimary)
            FilterDropdown("Línea", lines.map { it.label }, line?.label, { label = it ?: label })
            Text("${lineSamples.size} muestreo(s) en esta línea", fontSize = 12.sp, color = AquaTextSecondary)

            Text("Período A (base)", fontWeight = FontWeight.Bold, color = AquaPrimary)
            DateField("Desde", aStart, { aStart = it }, endOfDay = false)
            DateField("Hasta", aEnd, { aEnd = it }, endOfDay = true)
            Text("Período B (a comparar)", fontWeight = FontWeight.Bold, color = AquaPrimary)
            DateField("Desde", bStart, { bStart = it }, endOfDay = false)
            DateField("Hasta", bEnd, { bEnd = it }, endOfDay = true)

            if (result == null) {
                Text("Defina al menos un límite de fecha en cada período.", color = AquaTextSecondary, fontSize = 13.sp)
            } else {
                Card(
                    Modifier.fillMaxWidth(), RoundedCornerShape(12.dp),
                    CardDefaults.cardColors(containerColor = AquaSurface), CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resultado (B respecto de A)", fontWeight = FontWeight.Bold, color = AquaPrimary)
                        Text("A: ${result.base.samples} muestra(s) · ${result.base.individuals} individuos · ${fmt(result.base.density)}", color = AquaTextPrimary)
                        Text("B: ${result.current.samples} muestra(s) · ${result.current.individuals} individuos · ${fmt(result.current.density)}", color = AquaTextPrimary)
                        if (result.base.samples == 0 || result.current.samples == 0) {
                            Text("Hay un período sin muestras: no se puede calcular una variación válida.", color = StatusAlertDark, fontSize = 13.sp)
                        } else {
                            Text("Individuos: ${signed(result.individuals.absolute, 0)} (${pct(result.individuals.percent)})", fontWeight = FontWeight.SemiBold, color = AquaTextPrimary)
                            Text("Densidad: ${result.density?.let { signed(it.absolute, 3) + " ind/cm" } ?: "—"} (${pct(result.density?.percent)})", fontWeight = FontWeight.SemiBold, color = AquaTextPrimary)
                        }
                    }
                }
            }
        }
    }
}

private fun fmt(d: Double?) = d?.let { String.format(Locale.US, "%.3f ind/cm", it) } ?: "sin tramo"
private fun signed(v: Double, decimals: Int) = String.format(Locale.US, "%+.${decimals}f", v)
private fun pct(v: Double?) = v?.let { String.format(Locale.US, "%+.1f%%", it) } ?: "n/d (base 0)"
