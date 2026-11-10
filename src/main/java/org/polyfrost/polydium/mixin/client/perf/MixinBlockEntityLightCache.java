package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//?} else {
/*import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
*///?}
import net.minecraft.core.BlockPos;
//? if >= 26.1 {
import net.minecraft.world.level.BlockAndLightGetter;
//?} else {
/*import net.minecraft.world.level.BlockAndTintGetter;
*///?}
import net.minecraft.world.level.block.entity.BlockEntity;
import org.polyfrost.polydium.client.perf.BlockEntityLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >= 1.21.10 {
@Mixin(BlockEntityRenderState.class)
//?} else {
/*@Mixin(BlockEntityRenderDispatcher.class)
*///?}
public class MixinBlockEntityLightCache {
    @WrapOperation(
            //? if >= 1.21.10 {
            method = "extractBase",
            //?} else {
            /*method = "setupAndRender",
            *///?}
            //? if >= 26.2 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/LightCoordsUtil;getLightCoords(Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/core/BlockPos;)I"),
            //?} else if >= 26.1 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;getLightCoords(Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/core/BlockPos;)I"),
            *///?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"),
            *///?}
            require = 0
    )
    private static int polydium$cachedLight(
            //? if >= 26.1 {
            BlockAndLightGetter level,
            //?} else {
            /*BlockAndTintGetter level,
            *///?}
            BlockPos pos, Operation<Integer> original, @Local(argsOnly = true) BlockEntity blockEntity
    ) {
        int generation = BlockEntityLight.generation(blockEntity);
        int light = BlockEntityLight.cached(blockEntity, generation);
        if (light == -1) {
            light = original.call(level, pos);
            BlockEntityLight.store(blockEntity, generation, light);
        }
        return light;
    }
}
