package org.polyfrost.polydium.client.perf.gpuprofile;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
//? if >= 26.3 {
import com.mojang.renderpearl.api.device.DeviceInfo;
//?} else
/*import com.mojang.blaze3d.systems.DeviceInfo;*/
//?} elif >= 1.21.5 {
/*import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
*///?} else {
/*import com.mojang.blaze3d.platform.GlUtil;
*///?}
//? if >= 1.21.11 && < 26.2 {
/*import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanProfilerStamps;
*///?}

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

final class GpuProfileBackend {
    private GpuProfileBackend() {
    }

    static GpuStamps createStamps(int size) {
        //? if >= 26.2 {
        return new PoolStamps(size);
        //?} elif >= 1.21.11 {
        /*return VulkanActive.active ? new VulkanProfilerStamps(size) : new GlStamps(size);
        *///?} else {
        /*return new GlStamps(size);
        *///?}
    }

    static Map<String, String> describe() {
        Map<String, String> out = new LinkedHashMap<>();
        Minecraft minecraft = Minecraft.getInstance();
        FabricLoader loader = FabricLoader.getInstance();

        out.put("minecraft", version(loader, "minecraft"));
        out.put("polydium", version(loader, "polydium"));
        //? if >= 26.2 {
        DeviceInfo info = RenderSystem.getDevice().getDeviceInfo();

        out.put("backend", info.backendName());
        out.put("gpu", info.name());
        out.put("vendor", info.vendorName());
        out.put("driver", info.driverInfo());
        //?} elif >= 1.21.5 {
        /*GpuDevice device = RenderSystem.getDevice();

        out.put("backend", device.getBackendName());
        out.put("gpu", device.getRenderer());
        out.put("vendor", device.getVendor());
        out.put("driver", device.getVersion());
        *///?} else {
        /*out.put("backend", "OpenGL");
        out.put("gpu", GlUtil.getRenderer());
        out.put("vendor", GlUtil.getVendor());
        out.put("driver", GlUtil.getOpenGLVersion());
        *///?}
        //? if >= 1.21.11 && < 26.2
        /*out.put("polydium vk", VulkanActive.active ? "active" : "off");*/
        out.put("resolution", minecraft.getWindow().getWidth() + "x" + minecraft.getWindow().getHeight());
        out.put("vsync", String.valueOf(minecraft.options.enableVsync().get()));
        out.put("fps limit", String.valueOf(minecraft.options.framerateLimit().get()));
        out.put("shader pack", irisPack());

        return out;
    }

    static @Nullable String unsupported(Map<String, String> environment) {
        if ("OpenGL".equals(environment.get("backend")) && environment.getOrDefault("gpu", "").startsWith("Apple"))
            return "Apple's OpenGL driver returns no GPU timestamps. Switch to Vulkan to profile on this Mac.";

        return null;
    }

    static boolean passLevelOnly(Map<String, String> environment) {
        return environment.getOrDefault("driver", "").contains("MoltenVK") && environment.getOrDefault("gpu", "").startsWith("Apple");
    }

    private static String version(FabricLoader loader, String id) {
        return loader.getModContainer(id).map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("?");
    }

    private static String irisPack() {
        if (!FabricLoader.getInstance().isModLoaded("iris"))
            return "iris not installed";

        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);

            return Boolean.TRUE.equals(api.getMethod("isShaderPackInUse").invoke(instance)) ? "in use" : "off";
        } catch (ReflectiveOperationException | LinkageError error) {
            return "unknown";
        }
    }
}
