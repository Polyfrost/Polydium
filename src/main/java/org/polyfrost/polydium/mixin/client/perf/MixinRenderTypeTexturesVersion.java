package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import org.polyfrost.polydium.client.perf.TextureManagerVersion;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class MixinRenderTypeTexturesVersion implements TextureManagerVersion {

    @Unique
    private int polydium$version;

    @Inject(method = {"register", "release", "close"}, at = @At("HEAD"))
    private void polydium$changeTextures(CallbackInfo ci) {
        this.polydium$version ++;
    }

    @Override
    public int polydium$getVersion() {
        return this.polydium$version;
    }
}
//?}
