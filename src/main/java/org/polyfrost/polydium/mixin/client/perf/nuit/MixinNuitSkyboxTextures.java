package org.polyfrost.polydium.mixin.client.perf.nuit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if nuit {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.flashyreese.mods.nuit.api.skyboxes.Skybox;
import me.flashyreese.mods.nuit.api.skyboxes.SkyboxTextureProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.client.perf.NuitSkyTextures;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
//?}

@Pseudo
@Mixin(targets = "me.flashyreese.mods.nuit.SkyboxManager", remap = false)
public class MixinNuitSkyboxTextures {
    //? if nuit {
    @Shadow
    @Final
    private List<Skybox> activeSkyboxes;

    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;registerAndLoad(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/ReloadableTexture;)V", remap = true),
            require = 0
    )
    private void polydium$deferUpload(TextureManager manager, Identifier id, ReloadableTexture texture, Operation<Void> original) {
        NuitSkyTextures.manage(id);
    }

    @Inject(method = "clearSkyboxes", at = @At("HEAD"), require = 0)
    private void polydium$dropPendingTextures(CallbackInfo ci) {
        NuitSkyTextures.clear();
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void polydium$streamTextures(ClientLevel level, CallbackInfo ci) {
        NuitSkyTextures.tick();
        for (Skybox skybox : this.activeSkyboxes) {
            if (skybox instanceof SkyboxTextureProvider provider) {
                for (Identifier id : provider.getTexturesToRegister()) {
                    NuitSkyTextures.use(id);
                }
            }
        }
    }
    //?}
}
