package com.aldemar.aquasample.ui.viewmodel
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import org.junit.Assert.*
import org.junit.Test

class SampleFilterTest {
    private val a = SampleEntity(id=1, code="MUE-A", centerName="Huar", trainNumber="Tren 1", lineNumber="Línea 2",
        sampleDate="22-10-2026", sectionLengthMeters=1.0, operatorName="Ana", musselCount=20, observations="")
    private val b = a.copy(id=2, code="MUE-B", centerName="Calbuco", status="VALIDADO", operatorName="Luis")
    private val samples = listOf(a,b)
    @Test fun blankQueryShowsAll() { assertEquals(samples,SampleFilter.apply(samples,"   ",null,null)) }
    @Test fun searchesAcrossFieldsIgnoringCaseAndExtraSpaces() {
        assertEquals(listOf(a), SampleFilter.apply(samples,"  ANA   huar ", null, null))
    }
    @Test fun combinesSearchStatusAndCenter() {
        assertEquals(listOf(b), SampleFilter.apply(samples,"MUE",SampleStatus.VALIDADO,"Calbuco"))
        assertTrue(SampleFilter.apply(samples,"Ana",SampleStatus.VALIDADO,"Calbuco").isEmpty())
    }
    @Test fun unknownTextProducesEmptyResult() { assertTrue(SampleFilter.apply(samples,"missing",null,null).isEmpty()) }
}
