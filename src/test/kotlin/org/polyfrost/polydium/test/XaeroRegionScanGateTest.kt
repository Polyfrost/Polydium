package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.compat.XaeroRegionScanGate

class XaeroRegionScanGateTest {
    @Test
    fun `idle regions are scanned every 8th pass`() {
        val gate = XaeroRegionScanGate()
        val region = Any()
        val scans = (0 until 80).count { gate.scan(region).also { gate.endPass() } }
        assertEquals(10, scans)
    }

    @Test
    fun `regions stay scanned every pass while uploads are pending`() {
        val gate = XaeroRegionScanGate()
        val region = Any()
        while (!gate.scan(region)) gate.endPass()
        gate.markPending()
        gate.endPass()

        repeat(20) {
            assertEquals(true, gate.scan(region))
            gate.markPending()
            gate.endPass()
        }

        gate.scan(region)
        gate.endPass()
        val scans = (0 until 80).count { gate.scan(region).also { gate.endPass() } }
        assertEquals(10, scans)
    }
}
