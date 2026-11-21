package com.aldemar.aquasample.ui.viewmodel

import com.aldemar.aquasample.data.export.ReportRows
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class AnalyticsTest {
    private fun s(code: String, center: String = "Huar", train: String = "T1", line: String = "L1",
                  ts: Long = 1000, cm: Int = 100, count: Int = 50, st: SampleStatus = SampleStatus.VALIDADA,
                  obs: String = "ok", concession: String? = null) =
        SampleEntity(code = code, center = center, train = train, line = line, timestamp = ts, sectionLengthCm = cm,
            operatorName = "Ana", manualCount = count, observations = obs, reviewStatus = st, concession = concession)

    // ---- HistoryAnalytics ----
    @Test fun statsAggregateIndividualsAndDensity() {
        val st = HistoryAnalytics.stats(listOf(s("a", cm = 100, count = 50), s("b", cm = 100, count = 70)))
        assertEquals(2, st.samples); assertEquals(120, st.individuals); assertEquals(0.6, st.density!!, 1e-9)
        assertNull(HistoryAnalytics.stats(emptyList()).density)
    }
    @Test fun compareGivesAbsoluteAndPercentVariation() {
        val c = HistoryAnalytics.compare(listOf(s("a", count = 100)), listOf(s("b", count = 80)))
        assertEquals(-20.0, c.individuals.absolute, 1e-9); assertEquals(-20.0, c.individuals.percent!!, 1e-9)
        assertEquals(-0.2, c.density!!.absolute, 1e-9); assertEquals(-20.0, c.density!!.percent!!, 1e-9)
    }
    @Test fun compareHandlesZeroBaseAndEmptyPeriods() {
        val zero = HistoryAnalytics.compare(listOf(s("a", count = 0)), listOf(s("b", count = 10)))
        assertEquals(10.0, zero.individuals.absolute, 1e-9); assertNull(zero.individuals.percent)
        val empty = HistoryAnalytics.compare(emptyList(), listOf(s("b")))
        assertNull(empty.density); assertNull(empty.individuals.percent)
    }
    @Test fun densityComparisonUsesDifferentSectionLengths() {
        val c = HistoryAnalytics.compare(listOf(s("a", cm = 100, count = 100)), listOf(s("b", cm = 200, count = 100)))
        assertEquals(0.0, c.individuals.absolute, 1e-9); assertEquals(-50.0, c.density!!.percent!!, 1e-9)
    }
    @Test fun rangeIsInclusiveAndOpenEnded() {
        val l = listOf(s("a", ts = 10), s("b", ts = 20), s("c", ts = 30))
        assertEquals(listOf("b", "c"), HistoryAnalytics.inRange(l, 20, null).map { it.code })
        assertEquals(listOf("a", "b"), HistoryAnalytics.inRange(l, null, 20).map { it.code })
        assertEquals(listOf("b"), HistoryAnalytics.inRange(l, 20, 20).map { it.code })
        assertEquals(3, HistoryAnalytics.inRange(l, null, null).size)
    }
    @Test fun linesAreDistinctIgnoringCaseAndFilterByKey() {
        val l = listOf(s("a", center = "Huar", line = "L1"), s("b", center = "huar ", line = "l1"), s("c", line = "L2"))
        val keys = HistoryAnalytics.lines(l)
        assertEquals(2, keys.size)
        assertEquals(listOf("a", "b"), HistoryAnalytics.forLine(l, keys.first()).map { it.code })
    }
    @Test fun dayBoundsCoverTheWholeLocalDay() {
        val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { clear(); set(2026, Calendar.MARCH, 5) }.timeInMillis
        val start = HistoryAnalytics.localDayStart(utc); val end = HistoryAnalytics.localDayEnd(utc)
        val cal = Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH)); assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(24 * 3600 * 1000L - 1, end - start)
    }
    @Test fun advancedFilterCombinesAllCriteria() {
        val l = listOf(s("a", ts = 10), s("b", train = "T2", ts = 20), s("c", line = "L2", ts = 30),
            s("d", st = SampleStatus.OBSERVADA, ts = 40))
        val f = HistoryFilter(status = SampleStatus.VALIDADA, center = "huar", train = "t1", line = "l1", startDate = 5, endDate = 35)
        assertEquals(listOf("a"), SampleFilter.apply(l, f).map { it.code })
        assertEquals(4, SampleFilter.apply(l, HistoryFilter()).size)
    }

    // ---- DashboardMetrics ----
    @Test fun kpisCountStatesAndAverageDensity() {
        val k = DashboardMetrics.kpis(listOf(
            s("a", cm = 100, count = 50), s("b", cm = 200, count = 50, st = SampleStatus.PENDIENTE_REVISION),
            s("c", st = SampleStatus.OBSERVADA, count = 0)))
        assertEquals(3, k.totalSamples); assertEquals(100, k.totalIndividuals)
        assertEquals(1, k.validated); assertEquals(1, k.pending); assertEquals(1, k.observed)
        assertEquals((0.5 + 0.25 + 0.0) / 3, k.avgDensity!!, 1e-9)
        assertNull(DashboardMetrics.kpis(emptyList()).avgDensity)
    }
    @Test fun byCenterGroupsIgnoringCaseSortedByIndividuals() {
        val bars = DashboardMetrics.byCenter(listOf(s("a", center = "Huar", count = 10), s("b", center = "huar", count = 20),
            s("c", center = "Calbuco", count = 100)))
        assertEquals(listOf("Calbuco", "Huar"), bars.map { it.center })
        assertEquals(2, bars[1].samples); assertEquals(30, bars[1].individuals)
    }
    @Test fun evolutionIsChronologicalForTheSelectedLineOnly() {
        val l = listOf(s("b", ts = 20, count = 7), s("a", ts = 10, count = 5), s("x", line = "L9", ts = 15))
        val pts = DashboardMetrics.evolution(l, HistoryAnalytics.LineKey("Huar", "T1", "L1"))
        assertEquals(listOf(5, 7), pts.map { it.count }); assertEquals(0.05, pts[0].density, 1e-9)
    }

    // ---- ReportRows ----
    @Test fun onlyValidatedSamplesAreExported() {
        val l = listOf(s("a"), s("b", st = SampleStatus.PENDIENTE_REVISION), s("c", st = SampleStatus.OBSERVADA))
        assertEquals(listOf("a"), ReportRows.validated(l).map { it.code })
        assertTrue(ReportRows.validated(l.drop(1)).isEmpty())
    }
    @Test fun csvHasAllColumnsAndEscapesSpecialCharacters() {
        val csv = ReportRows.toCsv(listOf(s("M1", concession = "Norte", obs = "a, \"b\"\nlínea")), date = { "FECHA" })
        assertTrue(csv.startsWith("﻿ID muestra,Fecha y hora,Centro,Concesión,Tren,Línea,Tramo (cm),Conteo total,Operador,Observaciones,Estado\r\n"))
        assertTrue(csv.contains("M1,FECHA,Huar,Norte,T1,L1,100,50,Ana,\"a, \"\"b\"\"\nlínea\",Validada"))
    }
    @Test fun csvNeutralizesSpreadsheetFormulas() {
        assertEquals("'=SUM(A1)", ReportRows.csvCell("=SUM(A1)"))
        assertEquals("'@cmd", ReportRows.csvCell("@cmd"))
        assertEquals("normal", ReportRows.csvCell("normal"))
    }
    @Test fun pdfWrapNeverExceedsWidthAndKeepsAllText() {
        val lines = ReportRows.wrap("uno dos tres " + "x".repeat(50), 20)
        assertTrue(lines.all { it.length <= 20 })
        assertEquals("uno dos tres " + "x".repeat(50), lines.joinToString(" ").replace("x ", "x").trim())
        assertTrue(ReportRows.pdfLines(s("M1"), date = { "F" }).first().startsWith("M1"))
    }

    @Test fun pdfJustifyAndPaginationLimits() {
        val m: (String) -> Float = { it.length * 6f }
        val text = "uno dos tres cuatro cinco seis siete ocho nueve diez once doce trece catorce"
        val lines = com.aldemar.aquasample.data.export.ReportRows.justify(text, 120f, m)
        assertTrue(lines.size > 1)
        lines.dropLast(1).forEach { l ->
            assertEquals(120f, l.words.sumOf { m(it).toDouble() }.toFloat() + l.gap * (l.words.size - 1), 0.01f)
        }
        assertEquals(6f, lines.last().gap, 0.001f)
        assertTrue(com.aldemar.aquasample.data.export.ReportRows.wrapByWidth("x".repeat(50), 60f, m).all { it.length <= 10 })
        val layout = com.aldemar.aquasample.data.export.PdfLayout
        val few = layout.paginate(List(3) { 100f }, 150f, layout.noteHeight())
        assertEquals(1, few.pages.size); assertEquals(0, few.omitted)
        val many = layout.paginate(List(500) { 100f }, 150f, layout.noteHeight())
        assertEquals(5, many.pages.size)
        assertEquals(500, many.pages.sumOf { it.size } + many.omitted); assertTrue(many.omitted > 0)
    }
}
