package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 && < 26.3 {
/*import org.polyfrost.polydium.client.perf.FontGeneration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$CachedFontProvider")
public abstract class MixinFontProviderInvalidate {

    @Shadow
    public abstract void invalidate();

    @Unique
    private int polydium$generation;

    @Inject(method = "effect", at = @At("HEAD"))
    private void polydium$invalidateOnOptionChange(CallbackInfoReturnable<?> cir) {
        int generation = FontGeneration.get();
        if (this.polydium$generation != generation) {
            this.polydium$generation = generation;
            this.invalidate();
        }
    }
}
*///?}
