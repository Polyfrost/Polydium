package org.polyfrost.polydium.mixin.client;

//? if >= 1.21.11 {
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.client.VulkanPrompt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.device.BackendCreationException;
import org.jspecify.annotations.Nullable;
import org.polyfrost.polydium.client.VulkanAvailability;
//?}

@Mixin(Minecraft.class)
public abstract class MixinVulkanStartupProbe {
    // Right before the window is created, after vanilla 26.2+ has run its own probe for the Default graphics API
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createTitle()Ljava/lang/String;"))
    private void polydium$probeVulkan(CallbackInfo ci) {
        VulkanPrompt.probeAtStartup();
    }

    //? if >= 26.2 {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanBackend;checkBackendAvailable()Lcom/mojang/renderpearl/api/device/BackendCreationException;"))
    private @Nullable BackendCreationException polydium$recordVanillaProbe(@Nullable BackendCreationException unavailable) {
        VulkanAvailability.record(unavailable == null ? null : unavailable.getMessage());
        return unavailable;
    }
    //?}
}
//?}
