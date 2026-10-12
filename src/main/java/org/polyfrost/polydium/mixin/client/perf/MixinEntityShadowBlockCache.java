package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >= 26.1 {
import net.minecraft.client.renderer.Lightmap;
//?} else {
/*import net.minecraft.client.renderer.LightTexture;
*///?}
//? if >= 1.21.10 {
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Shadow;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.polyfrost.polydium.client.perf.EntityShadowBlockCache;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(/*? if >= 1.21.10 {*/EntityRenderer/*?} else {*//*EntityRenderDispatcher*//*?}*/.class)
public class MixinEntityShadowBlockCache {
    //? if >= 1.21.10 {
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
    //?} else {
    /*@Shadow
    private static void shadowVertex(PoseStack.Pose pose, VertexConsumer buffer, int color, float x, float y, float z, float u, float v) {
        throw new AssertionError();
    }

    @WrapMethod(method = "renderBlockShadow")
    private static void polydium$cachedBlockShadow(PoseStack.Pose pose, VertexConsumer buffer, ChunkAccess chunk, LevelReader level, BlockPos pos, double x, double y, double z, float radius, float pow, Operation<Void> original) {
        if (!RenderSystem.isOnRenderThread()) {
            original.call(pose, buffer, chunk, level, pos, x, y, z, radius, pow);
            return;
        }
        EntityShadowBlockCache.Entry entry = EntityShadowBlockCache.get(level, pos, chunk);
        if (entry == null) {
            return;
        }
        float alpha = pow * 0.5F * LightTexture.getBrightness(level.dimensionType(), entry.brightness());
        if (alpha < 0.0F) {
            return;
        }
        int color = Mth.floor(Math.min(alpha, 1.0F) * 255.0F) << 24 | 0xFFFFFF;
        AABB bounds = entry.shape().bounds();
        float minX = (float) (pos.getX() + bounds.minX - x);
        float maxX = (float) (pos.getX() + bounds.maxX - x);
        float minY = (float) (pos.getY() + bounds.minY - y);
        float minZ = (float) (pos.getZ() + bounds.minZ - z);
        float maxZ = (float) (pos.getZ() + bounds.maxZ - z);
        float u0 = -minX / 2.0F / radius + 0.5F;
        float u1 = -maxX / 2.0F / radius + 0.5F;
        float v0 = -minZ / 2.0F / radius + 0.5F;
        float v1 = -maxZ / 2.0F / radius + 0.5F;
        shadowVertex(pose, buffer, color, minX, minY, minZ, u0, v0);
        shadowVertex(pose, buffer, color, minX, minY, maxZ, u0, v1);
        shadowVertex(pose, buffer, color, maxX, minY, maxZ, u1, v1);
        shadowVertex(pose, buffer, color, maxX, minY, minZ, u1, v0);
    }
    *///?}
}
//?}
