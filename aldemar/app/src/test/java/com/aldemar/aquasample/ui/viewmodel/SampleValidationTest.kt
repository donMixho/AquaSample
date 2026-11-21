package com.aldemar.aquasample.ui.viewmodel
import org.junit.Assert.*
import org.junit.Test

class SampleValidationTest {
    private fun location(length: String) = SampleValidation.validateLocation("Huar", "T1", "L2", "Ana", length)
    @Test fun acceptsWholeCentimeterLengths() {
        for (length in listOf("100", " 150 ", "1", "100000")) assertTrue("rejected: $length", location(length).isEmpty())
    }
    @Test fun rejectsMissingRequiredFields() {
        assertEquals(setOf("center", "train", "line", "operator", "length"),
            SampleValidation.validateLocation(" ", "", "\t", "", "").keys)
    }
    @Test fun rejectsInvalidDecimalAndOutOfRangeLength() {
        for (value in listOf("abc", "0", "000", "-2", "NaN", "Infinity", "1e9", "1,2,3", "1.5", "1,5", "100 cm", "100001", "99999999999"))
            assertTrue("Unexpected acceptance: $value", location(value).containsKey("length"))
    }
    @Test fun lengthMessagesExplainWhatIsWrong() {
        assertNull(SampleValidation.validateLength("100"))
        assertTrue(SampleValidation.validateLength("").orEmpty().contains("centímetros"))
        assertTrue(SampleValidation.validateLength("1.5").orEmpty().contains("enteros"))
        assertTrue(SampleValidation.validateLength("0").orEmpty().contains("mayor que cero"))
        assertTrue(SampleValidation.validateLength("100001").orEmpty().contains("máxima"))
    }
    @Test fun concessionIsOptionalButBounded() {
        assertTrue(SampleValidation.validateLocation("Huar", "T1", "L2", "Ana", "100", "").isEmpty())
        assertTrue(SampleValidation.validateLocation("Huar", "T1", "L2", "Ana", "100", "Concesión Norte").isEmpty())
        assertTrue(SampleValidation.validateLocation("Huar", "T1", "L2", "Ana", "100", "C".repeat(101)).containsKey("concession"))
    }
    @Test fun rejectsExcessiveFieldLength() {
        assertTrue(SampleValidation.validateLocation("Huar", "T".repeat(101), "L2", "Ana", "100").containsKey("train"))
    }
    @Test fun acceptsZeroAndUpperCountBoundary() {
        for (value in listOf("0", "120", "1000000")) assertNull(SampleValidation.validateCount(value))
    }
    @Test fun rejectsInvalidCountWithoutSilentlyConvertingToZero() {
        for (value in listOf("", "-1", "abc", "1.5", "1000001", "999999999999999"))
            assertNotNull(SampleValidation.validateCount(value))
    }
    @Test fun observedSampleRequiresAnExplanation() {
        assertNotNull(SampleValidation.validateReview(true, "  "))
        assertNull(SampleValidation.validateReview(true, "Repetir conteo por diferencia con fotografía."))
    }
    @Test fun approvalAllowsOptionalCommentButEnforcesLength() {
        assertNull(SampleValidation.validateReview(false, ""))
        assertNotNull(SampleValidation.validateReview(false, "x".repeat(2001)))
    }
}
