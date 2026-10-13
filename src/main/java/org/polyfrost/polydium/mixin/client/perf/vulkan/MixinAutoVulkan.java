package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.ModifyReceiver;
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.device.BackendCreationException;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.PreferredGraphicsApi;
import org.polyfrost.polydium.client.perf.AutoVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MixinAutoVulkan {

    @ModifyReceiver(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/PreferredGraphicsApi;getBackendsToTry"))
    private PreferredGraphicsApi polydium$autoVulkan(PreferredGraphicsApi api) {
        return AutoVulkan.pick(api);
    }

    //? if >= 26.3 {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanBackend;checkBackendAvailable"))
    private BackendCreationException polydium$reuseProbe(Operation<BackendCreationException> original) {
        return AutoVulkan.probed ? AutoVulkan.probeResult : original.call();
    }
    //?}
}
//?}
