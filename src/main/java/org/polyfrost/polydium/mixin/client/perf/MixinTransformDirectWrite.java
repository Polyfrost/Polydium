package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
//? if >= 26.3 {
import net.minecraft.client.renderer.DynamicGpuData;
//?} else {
/*import net.minecraft.client.renderer.DynamicUniforms;
*///?}
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.polyfrost.polydium.client.perf.TransformWrites;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

//? if >= 26.3 {
@Mixin(DynamicGpuData.Transform.class)
//?} else {
/*@Mixin(DynamicUniforms.Transform.class)
*///?}
public abstract class MixinTransformDirectWrite {
    @Shadow
    @Final
    private Matrix4fc modelView;

    @Shadow
    @Final
    private Vector4fc colorModulator;

    @Shadow
    @Final
    private Vector3fc modelOffset;

    @Shadow
    @Final
    private Matrix4fc textureMatrix;

    //? if < 1.21.11 {
    /*@Shadow
    @Final
    private float lineWidth;
    *///?}

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void polydium$writeDirect(ByteBuffer buffer, CallbackInfo ci) {
        if (TransformWrites.write(buffer, this.modelView, this.colorModulator, this.modelOffset, this.textureMatrix/*? if < 1.21.11 {*//*, this.lineWidth*//*?}*/)) {
            ci.cancel();
        }
    }
}
//?}
