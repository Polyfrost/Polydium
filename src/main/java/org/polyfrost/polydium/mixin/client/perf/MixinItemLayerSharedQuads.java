package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 && < 26.3 {
/*import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client./^? if >= 26.1 {^/resources.model.geometry/^?} else {^//^renderer.block.model^//^?}^/.BakedQuad;
import org.polyfrost.polydium.client.perf.SharedItemQuads;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class MixinItemLayerSharedQuads implements SharedItemQuads {
    @Shadow
    @Final
    @Mutable
    private List<BakedQuad> quads;

    @Unique
    private List<BakedQuad> polydium$ownQuads;

    @Override
    public void polydium$shareQuads(List<BakedQuad> shared) {
        if (this.polydium$ownQuads == null) {
            this.polydium$ownQuads = this.quads;
        }
        this.quads = shared;
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void polydium$dropSharedQuads(CallbackInfo ci) {
        if (this.polydium$ownQuads != null) {
            this.quads = this.polydium$ownQuads;
            this.polydium$ownQuads = null;
        }
    }

    @Inject(method = "prepareQuadList", at = @At("HEAD"))
    private void polydium$copySharedQuads(CallbackInfoReturnable<List<BakedQuad>> cir) {
        if (this.polydium$ownQuads != null) {
            List<BakedQuad> shared = this.quads;
            this.quads = this.polydium$ownQuads;
            this.polydium$ownQuads = null;
            this.quads.addAll(shared);
        }
    }
}
*///?}
