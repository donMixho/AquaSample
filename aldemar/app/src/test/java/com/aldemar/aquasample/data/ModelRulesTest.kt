package com.aldemar.aquasample.data

import com.aldemar.aquasample.data.model.SampleStatus
import com.aldemar.aquasample.data.model.UserRole
import com.aldemar.aquasample.data.repository.RolePolicy
import com.aldemar.aquasample.ui.viewmodel.SampleFormat
import org.junit.Assert.*
import org.junit.Test

class ModelRulesTest {
    @Test fun analystCanConsultDashboardAndExportButNeverCreateOrReview() {
        val a = UserRole.ANALYST
        assertTrue(a.canViewHistory && a.canViewDashboard && a.canExport)
        assertFalse(a.canCreateSamples)
        assertFalse(a.canReview)
    }
    @Test fun onlyOperatorCreatesAndOnlySupervisorReviews() {
        assertEquals(listOf(UserRole.OPERATOR), UserRole.entries.filter { it.canCreateSamples })
        assertEquals(listOf(UserRole.SUPERVISOR), UserRole.entries.filter { it.canReview })
        assertFalse(UserRole.OPERATOR.canExport)
    }
    @Test fun unknownRoleNameFallsBackToLeastPrivilege() {
        assertEquals(UserRole.OPERATOR, UserRole.fromName(null))
        assertEquals(UserRole.OPERATOR, UserRole.fromName("admin"))
        assertEquals(UserRole.ANALYST, UserRole.fromName("ANALYST"))
    }
    @Test fun trustedClaimGrantsAnalystOnlyOnExactMatch() {
        assertEquals(UserRole.ANALYST, RolePolicy.fromClaims(mapOf("role" to "ANALYST")))
        assertEquals(UserRole.OPERATOR, RolePolicy.fromClaims(mapOf("role" to "analyst")))
    }
    @Test fun legacyStatusNamesMapToNewReviewStates() {
        assertEquals(SampleStatus.PENDIENTE_REVISION, SampleStatus.fromString("PENDIENTE"))
        assertEquals(SampleStatus.VALIDADA, SampleStatus.fromString("VALIDADO"))
        assertEquals(SampleStatus.OBSERVADA, SampleStatus.fromString("OBSERVADO"))
        assertEquals(SampleStatus.CORREGIDA, SampleStatus.fromString("corregida"))
    }
    @Test fun centimetersParseOnlyWholePositiveNumbersInRange() {
        assertEquals(100, SampleFormat.parseCm("100"))
        assertEquals(150, SampleFormat.parseCm(" 150 "))
        assertEquals(1, SampleFormat.parseCm("1"))
        assertEquals(SampleFormat.MAX_LENGTH_CM, SampleFormat.parseCm("100000"))
        for (bad in listOf("", "abc", "0", "-1", "1.5", "1,5", "NaN", "1e3", "100001", "99999999999"))
            assertNull("accepted: $bad", SampleFormat.parseCm(bad))
    }
    @Test fun lengthLabelShowsCentimeters() {
        assertEquals("1 m (100 cm)", SampleFormat.lengthLabel(100))
        assertEquals("150 cm", SampleFormat.lengthLabel(150))
    }
}
