package org.polyfrost.polydium.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//? if >= 26.3
import org.polyfrost.polydium.client.perf.IrisOverrideSweep
import org.polyfrost.polydium.client.perf.PerfClock
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfileCommand
//? if >= 1.21.11 && < 26.2
/*import org.polyfrost.polydium.vulkan.IrisVulkan*/

class PolydiumClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register {
            PerfClock.tick += 1
            VulkanPrompt.tick(it)
            //? if >= 26.3
            IrisOverrideSweep.tick()
        }

        PolydiumConfig.preload()
        GpuProfileCommand.register()
        //? if >= 1.21.11 && < 26.2
        /*IrisVulkan.init()*/
    }
}
