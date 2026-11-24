package org.polyfrost.polydium.test

//? if >= 1.21.11 {
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.RecyclePolling

class RecyclePollingTest {
    @Test
    fun `only the previous and current GL submits count as in flight`() {
        RecyclePolling.glSubmitIndex = -1L
        assertFalse(RecyclePolling.isGlInFlight(5L))

        RecyclePolling.glSubmitIndex = 10L
        assertFalse(RecyclePolling.isGlInFlight(8L))
        assertTrue(RecyclePolling.isGlInFlight(9L))
        assertTrue(RecyclePolling.isGlInFlight(10L))
    }
}
//?}
