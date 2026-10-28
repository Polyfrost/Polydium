package org.polyfrost.polydium.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.polyfrost.polydium.client.perf.PerfClock
//? if >= 1.21.11 && < 26.2
/*import org.polyfrost.polydium.vulkan.IrisVulkan*/

class PolydiumClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register {
            PerfClock.tick += 1
            VulkanPrompt.tick(it)
        }

        PolydiumConfig.preload()
        //? if >= 1.21.11 && < 26.2
        /*IrisVulkan.init()*/
    }
}
