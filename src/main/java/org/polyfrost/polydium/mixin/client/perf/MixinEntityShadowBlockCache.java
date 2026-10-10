package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >= 26.1 {
import net.minecraft.client.renderer.Lightmap;
//?} else {
/*import net.minecraft.client.renderer.LightTexture;
*///?}
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.polyfrost.polydium.client.perf.EntityShadowBlockCache;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public class MixinEntityShadowBlockCache {
    @WrapMethod(method = "extractShadowPiece")
    private void polydium$cachedShadowPiece(EntityRenderState state, Level level, float pow, BlockPos.MutableBlockPos pos, ChunkAccess chunk, Operation<Void> original) {
        if (!RenderSystem.isOnRenderThread()) {
            original.call(state, level, pow, pos, chunk);
            return;
        }
        EntityShadowBlockCache.Entry entry = EntityShadowBlockCache.get(level, pos, chunk);
        if (entry == null) {
            return;
        }
        float powerAtDepth = pow - (float) (state.y - pos.getY()) * 0.5F;
        //? if >= 26.1 {
        float alpha = Mth.clamp(powerAtDepth * 0.5F * Lightmap.getBrightness(level.dimensionType(), entry.brightness()), 0.0F, 1.0F);
        //?} else {
        /*float alpha = Mth.clamp(powerAtDepth * 0.5F * LightTexture.getBrightness(level.dimensionType(), entry.brightness()), 0.0F, 1.0F);
        *///?}
        float relativeX = (float) (pos.getX() - state.x);
        float relativeY = (float) (pos.getY() - state.y);
        float relativeZ = (float) (pos.getZ() - state.z);
        state.shadowPieces.add(new EntityRenderState.ShadowPiece(relativeX, relativeY, relativeZ, entry.shape(), alpha));
    }
}
//?}
