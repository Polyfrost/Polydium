package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.3 {
import net.minecraft.client.renderer.DynamicGpuData;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
//?} else {
/*import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.DynamicUniforms;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

//? if >= 26.3 {
@Mixin(DynamicGpuDataStorageMapped.class)
//?} else {
/*@Mixin(DynamicUniformStorage.class)
*///?}
public class MixinTransformEquality {
    //? if >= 26.3 {
    @WrapOperation(
        method = "writeData(Lnet/minecraft/client/renderer/DynamicGpuDataStorage$DynamicGpuData;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicGpuDataStorage$DynamicGpuData;equals(Ljava/lang/Object;)Z")
    )
    private boolean polydium$sameTransform(DynamicGpuDataStorage.DynamicGpuData last, Object data, Operation<Boolean> original) {
        if (last instanceof DynamicGpuData.Transform a && data instanceof DynamicGpuData.Transform b) {
    //?} else {
    /*@WrapOperation(
        method = "writeUniform",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniformStorage$DynamicUniform;equals(Ljava/lang/Object;)Z")
    )
    private boolean polydium$sameTransform(DynamicUniformStorage.DynamicUniform last, Object data, Operation<Boolean> original) {
        if (last instanceof DynamicUniforms.Transform a && data instanceof DynamicUniforms.Transform b) {
    *///?}
            return a == b
                || Objects.equals(a.modelOffset(), b.modelOffset())
                && Objects.equals(a.colorModulator(), b.colorModulator())
                && Objects.equals(a.modelView(), b.modelView())
                && Objects.equals(a.textureMatrix(), b.textureMatrix())/*? if < 1.21.11 {*//* && Float.compare(a.lineWidth(), b.lineWidth()) == 0*//*?}*/;
        }
        return original.call(last, data);
    }
}
//?}
