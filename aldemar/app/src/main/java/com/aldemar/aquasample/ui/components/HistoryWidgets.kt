package com.aldemar.aquasample.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aldemar.aquasample.data.export.ReportExporter
import com.aldemar.aquasample.data.export.ReportFormat
import com.aldemar.aquasample.data.export.ReportRows
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.ui.theme.AquaPrimary
import com.aldemar.aquasample.ui.viewmodel.HistoryAnalytics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Selector de fecha (día local). [endOfDay] devuelve el último milisegundo del día elegido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, millis: Long?, onChange: (Long?) -> Unit, endOfDay: Boolean, modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    Row(modifier) {
        OutlinedButton(onClick = { open = true }) {
            Text(millis?.let { "$label: " + SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it)) } ?: "$label: sin límite")
        }
        if (millis != null) IconButton(onClick = { onChange(null) }) {
            Icon(Icons.Default.Close, contentDescription = "Quitar $label")
        }
    }
    if (open) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onChange(if (endOfDay) HistoryAnalytics.localDayEnd(it) else HistoryAnalytics.localDayStart(it))
                    }
                    open = false
                }) { Text("Aceptar", color = AquaPrimary) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar", color = AquaPrimary) } }
        ) { DatePicker(state = state) }
    }
}

/** Lista desplegable con opción "Todos"; null = sin filtro. */
@Composable
fun FilterDropdown(label: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit,
                   modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { open = true }) { Text(selected?.let { "$label: $it" } ?: "$label: todos") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelect(null); open = false })
            options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); open = false }) }
        }
    }
}

/**
 * Botón de exportar: PDF o CSV (Excel) de las muestras VALIDADAS de [samples].
 * Si no hay ninguna, avisa por [onMessage] en lugar de generar un archivo vacío.
 */
@Composable
fun ExportMenuButton(samples: List<SampleEntity>, onMessage: (String) -> Unit) {
    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    fun run(format: ReportFormat) {
        open = false
        if (ReportRows.validated(samples).isEmpty()) {
            onMessage("No hay muestras validadas para exportar. Solo se exportan las validadas por el supervisor.")
            return
        }
        scope.launch {
            try {
                val intent: Intent = ReportExporter(context).export(samples, format)
                context.startActivity(intent)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { onMessage("No se pudo generar el reporte. Reintenta.") }
        }
    }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Default.FileDownload, contentDescription = "Exportar validadas") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Exportar PDF (validadas)") }, onClick = { run(ReportFormat.PDF) })
            DropdownMenuItem(text = { Text("Exportar CSV / Excel (validadas)") }, onClick = { run(ReportFormat.CSV) })
        }
    }
}
