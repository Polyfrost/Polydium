package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
//? if < 26.3 {
/*import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
*///?}
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import org.polyfrost.polydium.client.perf.OcclusionCuller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer", remap = false)
public abstract class MixinOcclusionSectionFilter {
    @WrapOperation(method = "fillCommandBuffer", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/ChunkRenderList;sectionsWithGeometryIterator(Z)Lnet/caffeinemc/mods/sodium/client/util/iterator/ByteIterator;"))
    private static ByteIterator polydium$skipHiddenSections(ChunkRenderList list, boolean reverse, Operation<ByteIterator> original) {
        return OcclusionCuller.filter(list, original.call(list, reverse));
    }

    @Inject(method = "fillCommandBuffer", at = @At("HEAD"))
    private static void polydium$startRefill(CallbackInfo ci, @Local(argsOnly = true) RenderRegion region, @Local(argsOnly = true) TerrainRenderPass pass) {
        OcclusionCuller.filling(region, pass);
    }

    @Inject(method = "fillCommandBuffer", at = @At("RETURN"))
    private static void polydium$timeRefill(CallbackInfo ci) {
        OcclusionCuller.filled();
    }

    //? if < 26.3 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void polydium$cullHiddenSections(CallbackInfo ci, @Local(argsOnly = true) ChunkRenderMatrices matrices, @Local(argsOnly = true) ChunkRenderListIterable lists, @Local(argsOnly = true) TerrainRenderPass pass, @Local(argsOnly = true) CameraTransform camera) {
        if (pass == DefaultTerrainRenderPasses.SOLID) {
            OcclusionCuller.cull(lists, matrices, camera.x, camera.y, camera.z);
        }
    }
    *///?}
}
//?}
