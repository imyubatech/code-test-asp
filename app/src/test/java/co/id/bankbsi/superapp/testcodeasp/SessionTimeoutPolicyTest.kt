package co.id.bankbsi.superapp.testcodeasp

import co.id.bankbsi.superapp.testcodeasp.utils.session.SessionTimeoutPolicy as Policy
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeoutPolicyTest {
    @Test fun newSessionHasFullTimeout() {
        assertEquals(Policy.TIMEOUT_MS, Policy.remainingMs(100L, 100L, 1, 1))
    }
    @Test fun elapsedTimeReducesRemainingSession() {
        assertEquals(Policy.TIMEOUT_MS - 1_000L, Policy.remainingMs(100L, 1_100L, 1, 1))
    }
    @Test fun sessionExpiresExactlyAtBoundary() {
        assertEquals(1L, Policy.remainingMs(100L, 100L + Policy.TIMEOUT_MS - 1, 1, 1))
        assertEquals(0L, Policy.remainingMs(100L, 100L + Policy.TIMEOUT_MS, 1, 1))
        assertEquals(0L, Policy.remainingMs(100L, 100L + Policy.TIMEOUT_MS + 1, 1, 1))
    }
    @Test fun rebootInvalidatesSession() {
        assertEquals(0L, Policy.remainingMs(100L, 200L, 1, 2))
    }
    @Test fun invalidClockValuesInvalidateSession() {
        assertEquals(0L, Policy.remainingMs(-1L, 200L, 1, 1))
        assertEquals(0L, Policy.remainingMs(300L, 200L, 1, 1))
    }
    @Test fun unavailableBootCountStillUsesElapsedTime() {
        assertEquals(Policy.TIMEOUT_MS - 100L, Policy.remainingMs(100L, 200L, -1, -1))
    }
}
