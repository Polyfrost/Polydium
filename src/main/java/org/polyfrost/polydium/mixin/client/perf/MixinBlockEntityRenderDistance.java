package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.polyfrost.polydium.client.perf.BlockEntityDistance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDistance {
    @WrapOperation(
            method = "tryExtractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z")
    )
    private boolean polydium$distanceWithoutVector(BlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, Vec3 cameraPosition, Operation<Boolean> original) {
        if (BlockEntityDistance.inheritsDefault(renderer)) {
            return BlockEntityDistance.shouldRender(renderer, blockEntity, cameraPosition);
        }
        return original.call(renderer, blockEntity, cameraPosition);
    }
}
//?}
