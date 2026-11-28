package org.polyfrost.polydium.mixin.client.perf.nuit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if nuit {
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.client.perf.NuitSkyTextures;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Pseudo
@Mixin(targets = "me.flashyreese.mods.nuit.render.NuitRenderBackend", remap = false)
public class MixinNuitTextureDraw {
    //? if nuit {
    @Inject(method = "drawTextured", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polydium$skipUnloadedTexture(RenderPipeline pipeline, MeshData meshData, GpuBufferSlice dynamicTransforms, String samplerName, Identifier texture, CallbackInfo ci) {
        if (!NuitSkyTextures.isReady(texture)) {
            meshData.close();
            ci.cancel();
        }
    }
    //?}
}
