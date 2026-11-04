package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import org.polyfrost.polydium.client.perf.SectionBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo", remap = false)
public abstract class MixinOcclusionBoundsInfo implements SectionBounds.Holder {
    @Unique
    private long polydium$bounds;
    @Unique
    private int polydium$animatedBounds;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$takeBounds(CallbackInfo ci) {
        if (BuiltSectionInfo.EMPTY != null) {
            this.polydium$animatedBounds = SectionBounds.takeAnimated();
            this.polydium$bounds         = SectionBounds.take();
        }
    }

    @Override
    public long polydium$getBounds() {
        return this.polydium$bounds;
    }

    @Override
    public int polydium$getAnimatedBounds() {
        return this.polydium$animatedBounds;
    }
}
//?}
