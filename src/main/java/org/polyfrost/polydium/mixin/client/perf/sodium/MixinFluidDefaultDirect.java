package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.fabric.render.FluidRendererImpl", remap = false)
public abstract class MixinFluidDefaultDirect {
    @Unique
    private static final ClassValue<Boolean> DEFAULT_RENDER_FLUID = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                return type.getMethod("renderFluid", FluidRenderer.class, BlockPos.class, BlockAndTintGetter.class,
                        FluidRenderer.Output.class, BlockState.class, FluidState.class).getDeclaringClass() == FluidRenderHandler.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    };

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/client/render/fluid/v1/FluidRendering;render(Lnet/minecraft/client/renderer/block/FluidRenderer;Lnet/fabricmc/fabric/api/client/render/fluid/v1/FluidRenderHandler;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/fabricmc/fabric/api/client/render/fluid/v1/FluidRendering$DefaultRenderer;)V"))
    private void polydium$renderDefaultDirect(FluidRenderer renderer, FluidRenderHandler handler, BlockAndTintGetter level, BlockPos pos,
                                              FluidRenderer.Output output, BlockState blockState, FluidState fluidState, FluidRendering.DefaultRenderer defaultRenderer) {
        if (DEFAULT_RENDER_FLUID.get(handler.getClass())) {
            defaultRenderer.render(renderer, handler, level, pos, output, blockState, fluidState);
        } else {
            FluidRendering.render(renderer, handler, level, pos, output, blockState, fluidState, defaultRenderer);
        }
    }
}
//?}
