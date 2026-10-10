package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if < 26.3
/*import com.llamalad7.mixinextras.sugar.Local;*/
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.GpuTexture;
//? if < 26.3
/*import net.minecraft.client.Minecraft;*/
//? if >= 26.1
import net.minecraft.client.renderer.GameRenderer;
//? if >= 26.3
import net.minecraft.client.renderer.state.GameRenderState;
//? if >= 26.2
import org.joml.Vector4fc;
import org.polyfrost.polydium.client.perf.MainClearCompat;
//? if >= 26.1 {
import org.spongepowered.asm.mixin.Final;
//?}
import org.spongepowered.asm.mixin.Mixin;
//? if >= 26.1 {
import org.spongepowered.asm.mixin.Shadow;
//?}
import org.spongepowered.asm.mixin.injection.At;

//? if >= 26.1 {
@Mixin(GameRenderer.class)
public abstract class MixinSkipFirstMainClear {
    @Shadow
    @Final
    //? if >= 26.3 {
    private GameRenderState gameRenderState;
    //?} else {
    /*private Minecraft minecraft;
    *///?}

    //? if >= 26.2 {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V"))
    //?} else {
    /*@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;ILcom/mojang/renderpearl/api/textures/GpuTexture;D)V"))
    *///?}
    //? if >= 26.3 {
    private void polydium$skipClearBeforeLevel(CommandEncoder encoder, GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, Operation<Void> original) {
        if (this.gameRenderState.shouldRenderLevel && !MainClearCompat.worldClearReplaced()) {
    //?} else if >= 26.2 {
    /*private void polydium$skipClearBeforeLevel(CommandEncoder encoder, GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, Operation<Void> original, @Local(argsOnly = true) boolean advanceGameTime) {
        if (this.minecraft.isGameLoadFinished() && advanceGameTime && this.minecraft.level != null && !MainClearCompat.worldClearReplaced()) {
    *///?} else {
    /*private void polydium$skipClearBeforeLevel(CommandEncoder encoder, GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, Operation<Void> original, @Local(argsOnly = true) boolean advanceGameTime) {
        if (this.minecraft.isGameLoadFinished() && advanceGameTime && this.minecraft.level != null && !MainClearCompat.worldClearReplaced()) {
    *///?}
            return;
        }

        original.call(encoder, colorTexture, clearColor, depthTexture, clearDepth);
    }
}
//?} else {
/*@Mixin(Minecraft.class)
public abstract class MixinSkipFirstMainClear {
    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/renderpearl/api/textures/GpuTexture;ILcom/mojang/renderpearl/api/textures/GpuTexture;D)V"))
    private void polydium$skipClearBeforeLevel(CommandEncoder encoder, GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, Operation<Void> original, @Local(argsOnly = true) boolean advanceGameTime) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (!minecraft.noRender && minecraft.isGameLoadFinished() && advanceGameTime && minecraft.level != null && !MainClearCompat.worldClearReplaced()) {
            return;
        }

        original.call(encoder, colorTexture, clearColor, depthTexture, clearDepth);
    }
}
*///?}
//?}
