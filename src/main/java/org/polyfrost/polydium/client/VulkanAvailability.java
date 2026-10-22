package org.polyfrost.polydium.client;

//? if >= 1.21.11 {
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
//?} else
/*import org.polyfrost.polydium.vulkan.VulkanBackend;*/

public final class VulkanAvailability {
    private static boolean checked;
    private static @Nullable String unavailableReason;

    private VulkanAvailability() {
    }

    public static void record(@Nullable String reason) {
        checked = true;
        unavailableReason = reason;
    }

    // Why the Vulkan backend can't start on this machine, or null if it can. Probes once if startup didn't
    public static @Nullable String unavailableReason() {
        if (!checked) {
            try {
                //? if >= 26.2 {
                if ("Vulkan".equalsIgnoreCase(RenderSystem.getDevice().getDeviceInfo().backendName())) {
                    record(null);
                    return null;
                }
                //?}
                var unavailable = VulkanBackend.checkBackendAvailable();
                record(unavailable == null ? null : unavailable.getMessage());
            } catch (Throwable t) {
                record(t.toString());
            }
        }

        return unavailableReason;
    }

    public static Component vulkanTooltip() {
        String reason = unavailableReason();
        Component tooltip = Component.translatable("options.graphicsApi.tooltip.vulkan");
        return reason == null ? tooltip : CommonComponents.joinLines(tooltip, CommonComponents.EMPTY,
                Component.translatable("options.graphicsApi.tooltip.vulkanUnavailable", reason).withStyle(ChatFormatting.RED));
    }
}
//?}
