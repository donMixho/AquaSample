package com.aldemar.aquasample.ui.viewmodel
import com.aldemar.aquasample.data.local.entity.SampleEntity
import com.aldemar.aquasample.data.model.SampleStatus
import org.junit.Assert.*
import org.junit.Test

class SampleFilterTest {
    private val a = SampleEntity(id=1, code="MUE-A", center="Huar", train="Tren 1", line="Línea 2",
        timestamp=1_792_000_000_000L, sectionLengthCm=100, operatorName="Ana", manualCount=20, observations="")
    private val b = a.copy(id=2, code="MUE-B", center="Calbuco", reviewStatus=SampleStatus.VALIDADA, operatorName="Luis")
    private val samples = listOf(a,b)
    @Test fun blankQueryShowsAll() { assertEquals(samples,SampleFilter.apply(samples,"   ",null,null)) }
    @Test fun searchesAcrossFieldsIgnoringCaseAndExtraSpaces() {
        assertEquals(listOf(a), SampleFilter.apply(samples,"  ANA   huar ", null, null))
    }
    @Test fun combinesSearchStatusAndCenter() {
        assertEquals(listOf(b), SampleFilter.apply(samples,"MUE",SampleStatus.VALIDADA,"Calbuco"))
        assertTrue(SampleFilter.apply(samples,"Ana",SampleStatus.VALIDADA,"Calbuco").isEmpty())
    }
    @Test fun unknownTextProducesEmptyResult() { assertTrue(SampleFilter.apply(samples,"missing",null,null).isEmpty()) }
}
