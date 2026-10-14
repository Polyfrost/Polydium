package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import java.util.function.Supplier;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
    "com.seibel.distanthorizons.common.AbstractModInitializer",
    "com.seibel.distanthorizons.common.wrappers.DependencySetup",
    "com.seibel.distanthorizons.common.render.blaze.util.BlazeDhVertexFormatUtil",
    "com.seibel.distanthorizons.core.util.math.Mat4f"
}, remap = false)
public class MixinVulkanBackendListDistantHorizons {
    @Inject(method = "tryCreateModCompatAccessor(Ljava/lang/String;Ljava/lang/Class;Ljava/util/function/Supplier;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void polydium$skipIris(String modId, Class<?> accessorClass, Supplier<?> accessorConstructor, CallbackInfo ci) {
        if ("iris".equals(modId))
            ci.cancel();
    }

    @Inject(method = "tryCreateModCompatAccessor([Ljava/lang/String;Ljava/lang/Class;Ljava/util/function/Supplier;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void polydium$skipIris(String[] modIds, Class<?> accessorClass, Supplier<?> accessorConstructor, CallbackInfo ci) {
        if (java.util.Arrays.asList(modIds).contains("iris"))
            ci.cancel();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = {"setRenderingApiBindings", "<clinit>"}, at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/config/types/ConfigEntry;get()Ljava/lang/Object;"), require = 0)
    private static Object polydium$blazeOnVulkan(@Coerce Object entry) throws ReflectiveOperationException {
        Object api = entry.getClass().getMethod("get").invoke(entry);
        return VulkanActive.active && api instanceof Enum<?> value ? Enum.valueOf((Class) value.getDeclaringClass(), "BLAZE_3D") : api;
    }
}
