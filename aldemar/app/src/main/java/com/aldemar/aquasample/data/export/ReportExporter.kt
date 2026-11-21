package com.aldemar.aquasample.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aldemar.aquasample.R
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.ui.viewmodel.SampleFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportFormat(val extension: String, val mime: String) {
    PDF("pdf", "application/pdf"), CSV("csv", "text/csv")
}

/** Contenido de los reportes (puro, testeable en JVM). Solo muestras VALIDADAS. */
object ReportRows {
    private val BOM = 0xFEFF.toChar().toString()
    val HEADERS = listOf("ID muestra", "Fecha y hora", "Centro", "Concesión", "Tren", "Línea",
        "Tramo (cm)", "Conteo total", "Operador", "Observaciones", "Estado")

    fun validated(samples: List<SampleEntity>) =
        samples.filter { it.reviewStatus == SampleStatus.VALIDADA }.sortedBy { it.timestamp }

    fun row(s: SampleEntity, date: (Long) -> String = SampleFormat::dateTime) = listOf(
        s.code, date(s.timestamp), s.center, s.concession.orEmpty(), s.train, s.line,
        s.sectionLengthCm.toString(), s.manualCount.toString(), s.operatorName, s.observations, s.reviewStatus.label)

    /** Entrecomilla si hace falta y neutraliza fórmulas (=, +, -, @) que Excel ejecutaría. */
    fun csvCell(value: String): String {
        val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t')) "'" + value else value
        return if (safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' })
            "\"" + safe.replace("\"", "\"\"") + "\"" else safe
    }

    /** CSV con BOM UTF-8 para que Excel respete las tildes. */
    fun toCsv(samples: List<SampleEntity>, date: (Long) -> String = SampleFormat::dateTime): String =
        BOM + (listOf(HEADERS) + validated(samples).map { row(it, date) })
            .joinToString("\r\n") { cells -> cells.joinToString(",") { csvCell(it) } } + "\r\n"

    /** Líneas de texto de un registro para el PDF, con ajuste de ancho. */
    fun pdfLines(s: SampleEntity, maxChars: Int = 120, date: (Long) -> String = SampleFormat::dateTime): List<String> {
        val head = "${s.code}  ·  ${date(s.timestamp)}  ·  ${s.reviewStatus.label}"
        val data = "Centro: ${s.center}  |  Concesión: ${s.concession ?: "—"}  |  Tren: ${s.train}  |  Línea: ${s.line}"
        val metrics = "Tramo: ${s.sectionLengthCm} cm  |  Conteo total: ${s.manualCount}  |  Operador: ${s.operatorName}"
        return listOf(head) + wrap(data, maxChars) + wrap(metrics, maxChars) + wrap("Observaciones: ${s.observations}", maxChars)
    }

    fun cardHead(s: SampleEntity, date: (Long) -> String = SampleFormat::dateTime) =
        "${s.code}  ·  ${date(s.timestamp)}  ·  ${s.reviewStatus.label}"

    /** Párrafos de la ficha tras el encabezado; el último (observaciones) se justifica. */
    fun cardParagraphs(s: SampleEntity) = listOf(
        "Centro: ${s.center}  |  Concesión: ${s.concession ?: "—"}  |  Tren: ${s.train}  |  Línea: ${s.line}",
        "Tramo: ${s.sectionLengthCm} cm  |  Conteo total: ${s.manualCount}  |  Operador: ${s.operatorName}",
        "Observaciones: ${s.observations}")

    fun summary(rows: List<SampleEntity>, day: (Long) -> String): String {
        val total = rows.sumOf { it.manualCount.toLong() }
        return "Este informe técnico de ALDEMAR SpA consolida ${rows.size} muestra(s) validada(s) por supervisión, " +
            "registradas entre el ${day(rows.first().timestamp)} y el ${day(rows.last().timestamp)} en " +
            "${rows.map { it.center }.distinct().size} centro(s) de cultivo. El conteo total acumulado es de $total " +
            "individuos, con un promedio de ${total / rows.size} por muestra."
    }

    /** Ajuste por ancho medido (las palabras demasiado largas se parten por caracteres). */
    fun wrapByWidth(text: String, maxWidth: Float, measure: (String) -> Float): List<String> {
        val out = mutableListOf<String>()
        var cur = ""
        for (word in text.replace(Regex("\\s+"), " ").trim().split(" ").filter { it.isNotEmpty() }) {
            var rest = word
            while (measure(rest) > maxWidth && rest.length > 1) {
                var n = rest.length - 1
                while (n > 1 && measure(rest.take(n)) > maxWidth) n--
                if (cur.isNotEmpty()) { out += cur; cur = "" }
                out += rest.take(n); rest = rest.drop(n)
            }
            cur = when {
                cur.isEmpty() -> rest
                measure("$cur $rest") <= maxWidth -> "$cur $rest"
                else -> { out += cur; rest }
            }
        }
        if (cur.isNotEmpty()) out += cur
        return out
    }

    class JustLine(val words: List<String>, val gap: Float)

    /** Justifica: reparte el espacio sobrante entre palabras salvo en la última línea del párrafo. */
    fun justify(text: String, width: Float, measure: (String) -> Float): List<JustLine> {
        val lines = wrapByWidth(text, width, measure)
        return lines.mapIndexed { i, l ->
            val words = l.split(" ")
            val gap = if (i == lines.lastIndex || words.size < 2) measure(" ")
                else (width - words.sumOf { measure(it).toDouble() }.toFloat()) / (words.size - 1)
            JustLine(words, gap)
        }
    }

    fun wrap(text: String, maxChars: Int): List<String> {
        val out = mutableListOf<String>()
        var cur = ""
        for (word in text.replace(Regex("\\s+"), " ").trim().split(" ")) {
            for (piece in word.chunked(maxChars)) {
                if (cur.isEmpty()) cur = piece
                else if (cur.length + 1 + piece.length <= maxChars) cur += " $piece"
                else { out += cur; cur = piece }
            }
        }
        if (cur.isNotEmpty()) out += cur
        return out
    }
}

/** Genera el archivo en cache/reports y devuelve el Intent de compartir (FileProvider + ACTION_SEND). */
class ReportExporter(context: Context) {
    private val app = context.applicationContext

    /** @throws IllegalStateException si no hay muestras validadas. */
    suspend fun export(samples: List<SampleEntity>, format: ReportFormat): Intent = withContext(Dispatchers.IO) {
        val rows = ReportRows.validated(samples)
        check(rows.isNotEmpty()) { "No hay muestras validadas para exportar." }
        val dir = File(app.cacheDir, "reports").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() } // solo se conserva el último reporte
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "aquasample_validadas_$stamp.${format.extension}")
        when (format) {
            ReportFormat.CSV -> file.writeText(ReportRows.toCsv(rows), Charsets.UTF_8)
            ReportFormat.PDF -> writePdf(rows, file)
        }
        val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType(format.mime).putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "AquaSample · muestras validadas")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            "Compartir reporte"
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun writePdf(rows: List<SampleEntity>, file: File) {
        val sans = Typeface.create("sans-serif", Typeface.NORMAL)
        val sansBold = Typeface.create("sans-serif", Typeface.BOLD)
        val body = Paint().apply { textSize = PdfLayout.BODY; typeface = sans; color = Color.BLACK; isAntiAlias = true }
        val bold = Paint(body).apply { typeface = sansBold }
        val title = Paint(bold).apply { textSize = PdfLayout.TITLE }
        val measure: (String) -> Float = body::measureText
        val boldMeasure: (String) -> Float = bold::measureText
        val textW = PdfLayout.WIDTH - 2 * PdfLayout.MARGIN
        val logo = runCatching { BitmapFactory.decodeResource(app.resources, R.drawable.logo_aquasample) }.getOrNull()
        val mark = Paint().apply { alpha = PdfLayout.WATERMARK_ALPHA; isAntiAlias = true; isFilterBitmap = true }

        val summary = ReportRows.summary(rows) { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it)) }
        val summaryLines = ReportRows.wrapByWidth(summary, textW, measure)
        val cards = rows.map { s ->
            val paras = ReportRows.cardParagraphs(s)
            paras to (1 + paras.sumOf { ReportRows.wrapByWidth(it, textW, measure).size }) * PdfLayout.LINE + PdfLayout.GAP
        }
        val headerH = PdfLayout.TITLE + 8 + PdfLayout.LINE + 10 + PdfLayout.LINE +
            summaryLines.size * PdfLayout.LINE + 14
        val plan = PdfLayout.paginate(cards.map { it.second }, headerH, PdfLayout.noteHeight())
        val total = plan.pages.size
        val doc = PdfDocument()
        try {
            plan.pages.forEachIndexed { idx, indices ->
                val page = doc.startPage(PdfDocument.PageInfo.Builder(PdfLayout.WIDTH, PdfLayout.HEIGHT, idx + 1).create())
                val c = page.canvas
                drawWatermark(c, logo, mark, bold)
                var y = PdfLayout.MARGIN
                if (idx == 0) {
                    c.drawText("ALDEMAR SpA — Informe Técnico de Muestreo Validado", PdfLayout.MARGIN, y + PdfLayout.TITLE, title)
                    y += PdfLayout.TITLE + 8
                    c.drawText("Generado: " + SampleFormat.dateTime(System.currentTimeMillis()), PdfLayout.MARGIN, y + PdfLayout.BODY, body)
                    y += PdfLayout.LINE + 10
                    c.drawText("Resumen ejecutivo", PdfLayout.MARGIN, y + PdfLayout.BODY, bold)
                    y += PdfLayout.LINE
                    y = drawJustified(c, summary, PdfLayout.MARGIN, y, textW, body, measure)
                    y += 14
                }
                for (n in indices) {
                    val paras = cards[n].first
                    c.drawText(ReportRows.cardHead(rows[n]), PdfLayout.MARGIN, y + PdfLayout.BODY, bold)
                    y += PdfLayout.LINE
                    paras.forEachIndexed { k, p ->
                        y = if (k == paras.lastIndex) drawJustified(c, p, PdfLayout.MARGIN, y, textW, body, measure)
                        else ReportRows.wrapByWidth(p, textW, measure).fold(y) { yy, line ->
                            c.drawText(line, PdfLayout.MARGIN, yy + PdfLayout.BODY, body); yy + PdfLayout.LINE }
                    }
                    y += PdfLayout.GAP
                }
                if (idx == total - 1 && plan.omitted > 0) {
                    val note = "Nota: el reporte se limitó a ${PdfLayout.MAX_PAGES} hojas; ${plan.omitted} muestra(s) " +
                        "validada(s) adicional(es) quedaron fuera. Use la exportación CSV para el detalle completo."
                    drawJustified(c, note, PdfLayout.MARGIN, y, textW, bold, boldMeasure)
                }
                val footer = "Página ${idx + 1} de $total"
                val fy = PdfLayout.HEIGHT - PdfLayout.MARGIN + 12
                c.drawText(footer, PdfLayout.WIDTH - PdfLayout.MARGIN - body.measureText(footer), fy, body)
                c.drawText("ALDEMAR SpA · AquaSample", PdfLayout.MARGIN, fy, body)
                doc.finishPage(page)
            }
            file.outputStream().use { doc.writeTo(it) }
        } finally { doc.close() }
    }

    /** Logo centrado con baja opacidad; si no hay PNG, sello vectorial de texto. */
    private fun drawWatermark(c: Canvas, logo: Bitmap?, mark: Paint, bold: Paint) {
        val cx = PdfLayout.WIDTH / 2f
        val cy = PdfLayout.HEIGHT / 2f
        if (logo != null && logo.width > 0) {
            val w = PdfLayout.WIDTH * 0.6f
            val h = w * logo.height / logo.width
            c.drawBitmap(logo, null, RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2), mark)
        } else {
            val stamp = Paint(bold).apply { alpha = PdfLayout.WATERMARK_ALPHA; textSize = 40f; textAlign = Paint.Align.CENTER }
            c.save(); c.rotate(-30f, cx, cy)
            c.drawText("AQUASAMPLE · ALDEMAR SpA", cx, cy, stamp)
            c.restore()
        }
    }

    /** Párrafo justificado (la última línea queda a la izquierda). Devuelve la nueva coordenada y. */
    private fun drawJustified(c: Canvas, text: String, x: Float, top: Float, width: Float, paint: Paint,
                              measure: (String) -> Float): Float {
        var y = top
        for (l in ReportRows.justify(text, width, measure)) {
            var cx = x
            for (w in l.words) { c.drawText(w, cx, y + PdfLayout.BODY, paint); cx += measure(w) + l.gap }
            y += PdfLayout.LINE
        }
        return y
    }
}

/** Medidas de página y paginación pura (Carta, máx. 5 hojas). */
object PdfLayout {
    const val WIDTH = 612
    const val HEIGHT = 792
    const val MARGIN = 45f
    const val TITLE = 16f
    const val BODY = 12f
    const val LINE = 15f
    const val GAP = 10f
    const val MAX_PAGES = 5
    const val WATERMARK_ALPHA = 22
    private const val FOOTER = 20f

    fun noteHeight() = 3 * LINE + GAP

    class Plan(val pages: List<List<Int>>, val omitted: Int)

    /** Reparte bloques (alturas) en páginas; trunca en [maxPages] reservando espacio para la nota final. */
    fun paginate(heights: List<Float>, firstTop: Float, noteH: Float, maxPages: Int = MAX_PAGES): Plan {
        val bottom = HEIGHT - MARGIN - FOOTER
        fun run(reserve: Float): Plan {
            val pages = mutableListOf(mutableListOf<Int>())
            var y = MARGIN + firstTop
            var i = 0
            while (i < heights.size) {
                val limit = if (pages.size == maxPages) bottom - reserve else bottom
                if (y + heights[i] > limit && pages.last().isNotEmpty()) {
                    if (pages.size == maxPages) break
                    pages += mutableListOf<Int>(); y = MARGIN
                    continue
                }
                pages.last() += i; y += heights[i]; i++
            }
            return Plan(pages, heights.size - i)
        }
        val plain = run(0f)
        return if (plain.omitted == 0) plain else run(noteH)
    }
}

