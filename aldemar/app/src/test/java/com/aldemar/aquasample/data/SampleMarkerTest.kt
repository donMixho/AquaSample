package com.aldemar.aquasample.data

import com.aldemar.aquasample.data.model.MarkerSerializer
import com.aldemar.aquasample.data.model.SampleCount
import com.aldemar.aquasample.data.model.SampleMarker
import org.junit.Assert.*
import org.junit.Test

class SampleMarkerTest {
    @Test fun markerRejectsOutOfRangeAndNonFiniteCoordinates() {
        assertNotNull(SampleMarker.ofOrNull(0f, 1f))
        assertNotNull(SampleMarker.ofOrNull(0.5f, 0.25f))
        for ((x, y) in listOf(-0.01f to 0.5f, 0.5f to 1.01f, Float.NaN to 0f, 0f to Float.POSITIVE_INFINITY))
            assertNull("accepted $x,$y", SampleMarker.ofOrNull(x, y))
    }

    @Test fun emptyListSerializesToEmptyArray() {
        assertEquals("[]", MarkerSerializer.toJson(emptyList()))
        assertEquals(emptyList<SampleMarker>(), MarkerSerializer.fromJsonOrNull("[]"))
        assertEquals(emptyList<SampleMarker>(), MarkerSerializer.fromJsonOrNull("  [ ]  "))
    }

    @Test fun serializesWithStableCompactFormat() {
        val json = MarkerSerializer.toJson(listOf(SampleMarker(0.25f, 0.5f), SampleMarker(1f, 0f)))
        assertEquals("""[{"x":0.25,"y":0.5},{"x":1.0,"y":0.0}]""", json)
    }

    @Test fun roundTripPreservesOrderAndValues() {
        val markers = listOf(SampleMarker(0.123456f, 0.987654f), SampleMarker(0f, 0f), SampleMarker(0.5f, 1f),
            SampleMarker(0.00001f, 0.99999f))
        val back = MarkerSerializer.fromJson(MarkerSerializer.toJson(markers))
        assertEquals(markers.size, back.size)
        markers.zip(back).forEach { (a, b) ->
            assertEquals(a.x, b.x, 0.00001f)
            assertEquals(a.y, b.y, 0.00001f)
        }
    }

    @Test fun roundTripOfLargeListKeepsEveryMarker() {
        val markers = (0 until 1500).map { SampleMarker((it % 100) / 100f, (it % 37) / 37f) }
        assertEquals(1500, MarkerSerializer.fromJson(MarkerSerializer.toJson(markers)).size)
    }

    @Test fun parserToleratesWhitespaceAndExponents() {
        val parsed = MarkerSerializer.fromJsonOrNull(""" [ { "x" : 0.5 , "y":1e-1 } , {"x":1,"y":0} ] """)
        assertEquals(listOf(SampleMarker(0.5f, 0.1f), SampleMarker(1f, 0f)), parsed)
    }

    @Test fun malformedJsonIsRejectedNotSilentlyTruncated() {
        val bad = listOf(
            null, "", "null", "{}", "[1,2]", "[{\"x\":0.5}]", "[{\"x\":0.5,\"y\":0.5},]",
            "[{\"x\":0.5,\"y\":0.5}", "[{\"x\":\"a\",\"y\":0.5}]", "[{\"x\":0.5,\"y\":0.5} garbage]",
            "[{\"y\":0.5,\"x\":0.5}]"
        )
        for (json in bad) {
            assertNull("accepted: $json", MarkerSerializer.fromJsonOrNull(json))
            assertTrue(MarkerSerializer.fromJson(json).isEmpty())
        }
    }

    @Test fun outOfRangeCoordinatesInJsonAreRejected() {
        assertNull(MarkerSerializer.fromJsonOrNull("""[{"x":1.5,"y":0.5}]"""))
        assertNull(MarkerSerializer.fromJsonOrNull("""[{"x":0.5,"y":-0.1}]"""))
        assertNull(MarkerSerializer.fromJsonOrNull("""[{"x":0.5,"y":0.5},{"x":2,"y":0}]"""))
    }

    @Test fun totalIsMarkersPlusAdjustment() {
        assertEquals(0, SampleCount.total(0, 0))
        assertEquals(12, SampleCount.total(10, 2))
        assertEquals(8, SampleCount.total(10, -2))
        assertEquals(5, SampleCount.total(0, 5))
    }

    @Test fun totalIsClampedToValidRange() {
        assertEquals(0, SampleCount.total(1, -5))
        assertEquals(SampleCount.MAX_COUNT, SampleCount.total(10, SampleCount.MAX_COUNT))
    }

    @Test fun adjustmentForReachesExactTargetWithoutLosingMarkers() {
        for ((target, marks) in listOf(23 to 0, 23 to 10, 5 to 10, 0 to 7, 0 to 0)) {
            val adj = SampleCount.adjustmentFor(target, marks)
            assertEquals(target, SampleCount.total(marks, adj))
        }
        assertEquals(-5, SampleCount.adjustmentFor(5, 10))
    }

    @Test fun normalizeKeepsTotalInsideRangeAfterUndoOrClear() {
        // 1 marca y ajuste -1 => total 0. Al deshacer la marca, el ajuste sube para no quedar en -1.
        assertEquals(0, SampleCount.normalizeAdjustment(0, -1))
        assertEquals(-3, SampleCount.normalizeAdjustment(3, -3))
        assertEquals(-3, SampleCount.normalizeAdjustment(3, -9))
        assertEquals(SampleCount.MAX_COUNT - 4, SampleCount.normalizeAdjustment(4, SampleCount.MAX_COUNT))
        for (marks in 0..5) for (adj in -8..8) {
            val n = SampleCount.normalizeAdjustment(marks, adj)
            assertTrue(marks + n in 0..SampleCount.MAX_COUNT)
            assertEquals(SampleCount.total(marks, n), marks + n)
        }
    }
}
