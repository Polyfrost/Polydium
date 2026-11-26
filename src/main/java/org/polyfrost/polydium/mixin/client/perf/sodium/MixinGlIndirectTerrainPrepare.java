package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.3 {
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SodiumGlIndirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer", remap = false)
public abstract class MixinGlIndirectTerrainPrepare {
    @Inject(method = "prepare", at = @At("HEAD"))
    private void polydium$beginIndirectPrepare(CallbackInfo ci) {
        SodiumGlIndirect.beginPrepare();
    }

    @Inject(method = "prepare", at = @At("TAIL"))
    private void polydium$flushIndirectCommands(CallbackInfo ci) {
        if (PolydiumToggles.glIndirectTerrain && SodiumGlIndirect.canPrepare()) {
            SodiumGlIndirect.flushPrepared();
        }
    }
}
//?} else if >= 26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import java.util.Iterator;
import net.caffeinemc.mods.sodium.client.gpu.device.batch.MultiDrawBatch;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SodiumGlIndirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer", remap = false)
public abstract class MixinGlIndirectTerrainPrepare {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;"))
    private void polydium$prepareIndirectCommands(CallbackInfo ci, @Local(argsOnly = true) ChunkRenderListIterable renderLists,
                                                  @Local(argsOnly = true) TerrainRenderPass renderPass) {
        SodiumGlIndirect.beginPrepare();
        if (!PolydiumToggles.glIndirectTerrain || SodiumGlIndirect.irisTessellation || !SodiumGlIndirect.canPrepare()) {
            return;
        }

        Iterator<ChunkRenderList> iterator = renderLists.iterator(renderPass.isTranslucent());
        while (iterator.hasNext()) {
            RenderRegion region = iterator.next().getRegion();
            if (region.getStorage(renderPass) == null || region.getResources() == null) {
                continue;
            }

            MultiDrawBatch batch = region.getCachedBatch(renderPass);
            if (!batch.isEmpty() && batch instanceof SodiumGlIndirect.Batch indirect) {
                SodiumGlIndirect.queue(indirect);
            }
        }

        SodiumGlIndirect.flushPrepared();
    }
}
*///?}
