package org.polyfrost.polydium.early;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
//? if >= 1.21.11 {
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.SharedLibrary;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF16;
//?}

// Kept apart from EarlyVulkan: the JVM remembers a failed class lookup per calling class, and EarlyVulkan loads before oshi can
public final class EarlyVulkanPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        //? if >= 1.21.11
        showSwitchableGpus();
        detect(/*? if >= 26.2 {*/true/*?} else {*//*false*//*?}*/);
    }

    public void beforeMixinSelection() {
        detect(true);
    }

    private void detect(boolean decide) {
        //? if >= 1.21.11 {
        if (EarlyVulkan.discreteGpuKnown())
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

    //? if >= 1.21.11 {
    private static void showSwitchableGpus() {
        if (!System.getProperty("os.name", "").startsWith("Windows"))
            return;
        try (SharedLibrary kernel32 = APIUtil.apiCreateLibrary("kernel32")) {
            if (windowsPrefersPowerSaving())
                return;
            long setEnv = kernel32.getFunctionAddress("SetEnvironmentVariableW");
            for (String name : new String[]{"DISABLE_LAYER_NV_OPTIMUS_1", "DISABLE_LAYER_AMD_SWITCHABLE_GRAPHICS_1"}) {
                ByteBuffer key = memUTF16(name);
                ByteBuffer value = memUTF16("1");
                JNI.callPPI(memAddress(key), memAddress(value), setEnv);
                memFree(key);
                memFree(value);
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean windowsPrefersPowerSaving() throws IOException {
        String exe = ProcessHandle.current().info().command().orElse(null);
        if (exe == null)
            return false;
        Process reg = new ProcessBuilder("reg", "query", "HKCU\\Software\\Microsoft\\DirectX\\UserGpuPreferences", "/v", exe).redirectErrorStream(true).start();
        return new String(reg.getInputStream().readAllBytes()).contains("GpuPreference=1;");
    }
    //?}
}
