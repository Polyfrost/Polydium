package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.polyfrost.polydium.client.perf.OcclusionCuller;
import org.polyfrost.polydium.client.perf.OcclusionDepth;
import org.polyfrost.polydium.client.perf.RegionOcclusion;
import org.polyfrost.polydium.client.perf.SectionBounds;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSection", remap = false)
public abstract class MixinOcclusionBoundsSection {
    @Shadow @Final private RenderRegion region;

    @Shadow
    public abstract int getSectionIndex();

    @Inject(method = "setInfo", at = @At("HEAD"))
    private void polydium$storeBounds(BuiltSectionInfo info, CallbackInfoReturnable<?> cir) {
        SectionBounds.Holder holder = (SectionBounds.Holder) (Object) info;

        this.polydium$replaceBounds(info == null ? 0L : holder.polydium$getBounds(), info == null ? 0 : holder.polydium$getAnimatedBounds());
    }

    @Inject(method = "delete", at = @At("HEAD"))
    private void polydium$forgetBounds(CallbackInfo ci) {
        this.polydium$replaceBounds(0L, 0);
    }

    @Unique
    private void polydium$replaceBounds(long built, int animated) {
        RegionOcclusion occlusion = (RegionOcclusion) this.region;
        long[]          bounds    = occlusion.polydium$getSectionBounds();
        int             section   = this.getSectionIndex();
        long            previous  = bounds[section];

        occlusion.polydium$getAnimatedBounds()[section] = animated;

        if (previous == built)
            return;

        bounds[section] = built;

        boolean hidden = (occlusion.polydium$getHiddenSections()[section >>> 6] >>> section & 1L) != 0L;

        if ((int) previous != (int) built) {
            occlusion.polydium$getChangedAt()[section] = OcclusionDepth.captureCount();

            if (hidden)
                OcclusionCuller.hiddenRebuilt(this.region, section);
            else
                OcclusionCuller.boundsChanged(this.region, section);
        }

        if ((int) previous != 0 && previous >>> Integer.SIZE != built >>> Integer.SIZE)
            OcclusionCuller.occluderChanged(this.region, section, (int) previous, hidden ? occlusion.polydium$getHiddenAfter() : Long.MAX_VALUE);
    }
}
//?}
