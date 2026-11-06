package org.polyfrost.polydium.early;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
//? if >= 1.21.11 {
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
//?}

// Kept apart from EarlyVulkan: the JVM remembers a failed class lookup per calling class, and EarlyVulkan loads before oshi can
public final class EarlyVulkanPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        detect(/*? if >= 26.2 {*/true/*?} else {*//*false*//*?}*/);
    }

    public void beforeMixinSelection() {
        detect(true);
    }

    private void detect(boolean decide) {
        //? if >= 1.21.11 {
        if (Boolean.getBoolean("polydium.disableAll") || EarlyVulkan.discreteGpuKnown())
            return;
        try {
            boolean discrete = false;
            for (GraphicsCard card : new SystemInfo().getHardware().getGraphicsCards())
                discrete |= EarlyVulkan.isDiscreteName(card.getVendor() + " " + card.getName());
            EarlyVulkan.recordDiscreteGpu(discrete);
            // Older versions must not change the backend after their Vulkan mixins were selected.
            if (decide)
                new EarlyVulkan();
        } catch (Throwable ignored) {
        }
        //?}
    }
}
