package com.aldemar.aquasample.ui.viewmodel
import org.junit.Assert.*
import org.junit.Test

class SampleValidationTest {
    private fun location(length: String) = SampleValidation.validateLocation("Huar", "T1", "L2", "Ana", length)
    @Test fun acceptsPositiveLengthWithEitherDecimalSeparator() {
        for (length in listOf("1.5", " 1,5 ", "0.01")) assertTrue(location(length).isEmpty())
    }
    @Test fun rejectsMissingRequiredFields() {
        assertEquals(setOf("center", "train", "line", "operator", "length"),
            SampleValidation.validateLocation(" ", "", "\t", "", "").keys)
    }
    @Test fun rejectsInvalidAndNonFiniteLength() {
        for (value in listOf("abc", "0", "-2", "NaN", "Infinity", "1e999", "1,2,3"))
            assertTrue("Unexpected acceptance: $value", location(value).containsKey("length"))
    }
    @Test fun rejectsExcessiveFieldLength() {
        assertTrue(SampleValidation.validateLocation("Huar", "T".repeat(101), "L2", "Ana", "1").containsKey("train"))
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
