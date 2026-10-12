package org.polyfrost.polydium.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.polyfrost.polydium.client.perf.PerfClock

class PolydiumClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { PerfClock.tick += 1 }

        PolydiumConfig.preload()
    }
}
