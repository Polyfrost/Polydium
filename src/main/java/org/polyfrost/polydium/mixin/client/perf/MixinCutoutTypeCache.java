package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
*///?}
import org.polyfrost.polydium.client.perf.CutoutTypeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(/*? if >= 1.21.11 {*/RenderTypes/*?} else {*//*RenderType*//*?}*/.class)
public abstract class MixinCutoutTypeCache {
    //? if >= 26.1 {
    @WrapOperation(method = "entityCutout(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entityCutout(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType polydium$cachedCutout(Identifier texture, boolean affectsOutline, Operation<RenderType> original) {
    //?} else if >= 1.21.11 {
    /*@WrapOperation(method = "entityCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entityCutoutNoCull(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType polydium$cachedCutout(Identifier texture, boolean affectsOutline, Operation<RenderType> original) {
    *///?} else {
    /*@WrapOperation(method = "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType polydium$cachedCutout(ResourceLocation texture, boolean affectsOutline, Operation<RenderType> original) {
    *///?}
        RenderType type = (RenderType) CutoutTypeCache.get(texture);
        if (type == null) {
            type = original.call(texture, affectsOutline);
            CutoutTypeCache.put(texture, type);
        }
        return type;
    }
}
