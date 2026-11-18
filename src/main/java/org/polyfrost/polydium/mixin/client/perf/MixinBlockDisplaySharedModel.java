package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelSet;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.polyfrost.polydium.client.perf.FrameModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DisplayRenderer.BlockDisplayRenderer.class)
public abstract class MixinBlockDisplaySharedModel {
    @WrapOperation(
        method = "extractRenderState(Lnet/minecraft/world/entity/Display$BlockDisplay;Lnet/minecraft/client/renderer/entity/state/BlockDisplayEntityRenderState;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelResolver;update(Lnet/minecraft/client/renderer/block/BlockModelRenderState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/model/BlockDisplayContext;)V")
    )
    private void polydium$shareModel(BlockModelResolver resolver, BlockModelRenderState renderState, BlockState blockState, BlockDisplayContext context, Operation<Void> original, @Local(argsOnly = true) BlockDisplayEntityRenderState state) {
        BlockModelSet current = Minecraft.getInstance().getModelManager().getBlockModelSet();
        Map<BlockState, BlockModelRenderState> models = ((FrameModels.Owner) current).polydium$displayModels();
        BlockModelRenderState shared = models.get(blockState);
        if (shared == null) {
            shared = new BlockModelRenderState();
            original.call(resolver, shared, blockState, context);
            models.put(blockState, shared);
        }
        ((BlockDisplayRenderStateAccessor) state).polydium$setBlockModel(shared);
    }
}
//?}
