package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
//? if >= 26.3
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import net.minecraft.client.PreferredGraphicsApi;
import org.slf4j.LoggerFactory;

public final class AutoVulkan {

    public static boolean discrete;

    public static boolean available;

    //? if >= 26.3 {
    public static boolean probed;

    public static BackendCreationException probeResult;
    //?}

    private AutoVulkan() {
    }

    public static PreferredGraphicsApi pick(PreferredGraphicsApi api) {
        if (api != PreferredGraphicsApi.DEFAULT)
            return api;

        //? if >= 26.3 {
        probeResult = VulkanBackend.checkBackendAvailable();
        probed = true;
        //?}

        boolean appleArm = System.getProperty("os.name", "").startsWith("Mac") && "aarch64".equals(System.getProperty("os.arch"));
        if (!available || !appleArm && !discrete)
            return api;

        LoggerFactory.getLogger("polydium/perf").info("Graphics API is Default on {}: trying Vulkan first", appleArm ? "Apple Silicon" : "a discrete GPU");
        return PreferredGraphicsApi.VULKAN;
    }
}
//?}
