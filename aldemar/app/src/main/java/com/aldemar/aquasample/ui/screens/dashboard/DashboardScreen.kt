package com.aldemar.aquasample.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.ui.components.AquaTopBar
import com.aldemar.aquasample.ui.components.ExportMenuButton
import com.aldemar.aquasample.ui.components.FilterDropdown
import com.aldemar.aquasample.ui.theme.*
import com.aldemar.aquasample.ui.viewmodel.AnalyticsViewModel
import com.aldemar.aquasample.ui.viewmodel.DashboardMetrics
import com.aldemar.aquasample.ui.viewmodel.HistoryAnalytics
import com.aldemar.aquasample.ui.viewmodel.SampleFormat
import java.util.Locale

@Composable
fun DashboardScreen(role: UserRole, viewModel: AnalyticsViewModel, onNavigateBack: () -> Unit, onCompare: () -> Unit) {
    val samples by viewModel.samples.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); message = null } }

    val kpis = remember(samples) { DashboardMetrics.kpis(samples) }
    val bars = remember(samples) { DashboardMetrics.byCenter(samples) }
    val lines = remember(samples) { HistoryAnalytics.lines(samples) }
    var selectedLabel by rememberSaveable { mutableStateOf<String?>(null) }
    val line = lines.firstOrNull { it.label == selectedLabel } ?: lines.firstOrNull()
    val points = remember(samples, line) { line?.let { DashboardMetrics.evolution(samples, it) }.orEmpty() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                androidx.compose.material3.Snackbar(
                    containerColor = com.aldemar.aquasample.ui.theme.AquaPrimary,
                    contentColor = com.aldemar.aquasample.ui.theme.AquaSurface,
                    actionContentColor = com.aldemar.aquasample.ui.theme.AquaSurface,
                    dismissActionContentColor = com.aldemar.aquasample.ui.theme.AquaSurface
                ) {
                    Text(data.visuals.message, color = com.aldemar.aquasample.ui.theme.AquaSurface)
                }
            }
        },
        topBar = {
            AquaTopBar(title = "Dashboard", canNavigateBack = true, onNavigateBack = onNavigateBack, actions = {
                if (role.canExport) ExportMenuButton(samples) { message = it }
            })
        },
        containerColor = AquaBackground
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (samples.isEmpty()) Text("Aún no hay muestras para analizar.", color = AquaTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kpi("Muestras", kpis.totalSamples.toString())
                Kpi("Individuos contados", kpis.totalIndividuals.toString())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kpi("Densidad promedio", kpis.avgDensity?.let { String.format(Locale.US, "%.2f ind/cm", it) } ?: "—")
                Kpi("Validadas", kpis.validated.toString(), StatusValidated)
                Kpi("Pendientes", kpis.pending.toString(), AquaPrimary)
            }

            ChartCard("Muestras e individuos por centro") {
                if (bars.isEmpty()) Text("Sin datos.", color = AquaTextSecondary, fontSize = 13.sp)
                val maxSamples = bars.maxOfOrNull { it.samples }?.coerceAtLeast(1) ?: 1
                val maxInd = bars.maxOfOrNull { it.individuals }?.coerceAtLeast(1) ?: 1
                bars.forEach { b ->
                    Text(b.center, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AquaTextPrimary)
                    BarRow("Muestras", b.samples, b.samples / maxSamples.toFloat(), AquaSecondary)
                    BarRow("Individuos", b.individuals, b.individuals / maxInd.toFloat(), AquaPrimary)
                    Spacer(Modifier.height(4.dp))
                }
            }

            ChartCard("Evolución del conteo de una línea") {
                if (lines.isEmpty()) Text("Sin datos.", color = AquaTextSecondary, fontSize = 13.sp)
                else {
                    FilterDropdown("Línea", lines.map { it.label }, line?.label, { selectedLabel = it })
                    LineChart(points.map { it.timestamp to it.count })
                    if (points.isNotEmpty()) Text(
                        "${SampleFormat.dateTime(points.first().timestamp)}  →  ${SampleFormat.dateTime(points.last().timestamp)}  ·  máx. ${points.maxOf { it.count }}  ·  ${points.size} muestreo(s)",
                        fontSize = 11.sp, color = AquaTextSecondary
                    )
                }
            }
            androidx.compose.material3.OutlinedButton(onClick = onCompare, modifier = Modifier.fillMaxWidth()) {
                Text("Comparar períodos de una línea")
            }
        }
    }
}

@Composable
private fun RowScope.Kpi(title: String, value: String, accent: Color = AquaPrimary) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AquaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(title, fontSize = 11.sp, color = AquaTextSecondary)
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AquaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AquaPrimary)
            content()
        }
    }
}

/** Barra horizontal dibujada en Canvas con su valor a la derecha. */
@Composable
private fun BarRow(label: String, value: Int, fraction: Float, color: Color) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 11.sp, color = AquaTextSecondary, modifier = Modifier.padding(end = 0.dp).fillMaxWidth(0.22f))
        Canvas(Modifier.weight(1f).height(12.dp)) {
            val r = CornerRadius(6.dp.toPx())
            drawRoundRect(AquaOutline.copy(alpha = 0.4f), size = size, cornerRadius = r)
            drawRoundRect(color, size = Size((size.width * fraction.coerceIn(0f, 1f)).coerceAtLeast(if (value > 0) 4.dp.toPx() else 0f), size.height), cornerRadius = r)
        }
        Text(value.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AquaTextPrimary)
    }
}

/** Gráfico de línea (tiempo × conteo) en Canvas. */
@Composable
private fun LineChart(points: List<Pair<Long, Int>>) {
    Canvas(Modifier.fillMaxWidth().height(170.dp)) {
        val pad = 12.dp.toPx()
        val w = size.width - 2 * pad
        val h = size.height - 2 * pad
        repeat(5) { i ->
            val y = pad + h * i / 4f
            drawLine(AquaOutline, Offset(pad, y), Offset(pad + w, y), strokeWidth = 1.dp.toPx())
        }
        if (points.isEmpty()) return@Canvas
        val minT = points.first().first
        val spanT = (points.last().first - minT).coerceAtLeast(1L)
        val maxY = points.maxOf { it.second }.coerceAtLeast(1)
        fun pos(p: Pair<Long, Int>) = Offset(
            if (points.size == 1) pad + w / 2 else pad + w * (p.first - minT) / spanT.toFloat(),
            pad + h * (1f - p.second / maxY.toFloat())
        )
        val path = Path()
        points.forEachIndexed { i, p -> pos(p).let { if (i == 0) path.moveTo(it.x, it.y) else path.lineTo(it.x, it.y) } }
        drawPath(path, AquaPrimary, style = Stroke(width = 2.5.dp.toPx()))
        points.forEach { drawCircle(AquaSecondary, 5.dp.toPx(), pos(it)); drawCircle(AquaPrimary, 3.dp.toPx(), pos(it)) }
    }
}
