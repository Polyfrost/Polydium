package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import net.minecraft.client.renderer.texture.TextureManager;
import org.polyfrost.polydium.client.perf.GuiItemBake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class MixinGuiItemBakeTick {
    @Inject(method = "tick", at = @At("HEAD"))
    private void polydium$countTextureTick(CallbackInfo ci) {
        GuiItemBake.textureTick++;
    }
}
//?}
