package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 && < 26.1 {
/*import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.polyfrost.polydium.client.perf.SpriteSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSection", remap = false)
public abstract class MixinSpriteSetSection {
    @Shadow @Final private RenderRegion region;

    @Inject(method = {"setRenderState", "clearRenderState"}, at = @At("TAIL"), require = 0)
    private void polydium$forgetRegionSprites(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this.region instanceof SpriteSet.Access access) {
            access.polydium$forgetSprites();
        }
    }
}
*///?}
