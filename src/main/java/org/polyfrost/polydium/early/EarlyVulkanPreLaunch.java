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
        //? if >= 1.21.11 {
        if (Boolean.getBoolean("polydium.disableAll") || EarlyVulkan.discreteGpuKnown())
            return;
        try {
            boolean discrete = false;
            for (GraphicsCard card : new SystemInfo().getHardware().getGraphicsCards())
                discrete |= EarlyVulkan.isDiscreteName(card.getVendor() + " " + card.getName());
            EarlyVulkan.recordDiscreteGpu(discrete);
            // 26.2+ reads options.txt after this, so the run that had no GPU result can still finish this launch.
            // Older versions choose their mixins before now and pick this up on the next launch.
            //? if >= 26.2
            new EarlyVulkan();
        } catch (Throwable ignored) {
        }
        //?}
    }
}
