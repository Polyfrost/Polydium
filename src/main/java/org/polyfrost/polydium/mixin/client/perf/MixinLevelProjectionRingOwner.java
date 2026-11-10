package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import net.minecraft.client.renderer.GameRenderer;
//? if >= 26.1 {
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//?} else {
/*import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
*///?}
import org.polyfrost.polydium.client.perf.LevelProjectionRing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinLevelProjectionRingOwner {
    //? if >= 26.1 {
    @Shadow @Final
    private ProjectionMatrixBuffer levelProjectionMatrixBuffer;
    //?} else {
    /*@Shadow @Final
    private PerspectiveProjectionMatrixBuffer levelProjectionMatrixBuffer;
    *///?}

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$markLevelProjection(CallbackInfo ci) {
        ((LevelProjectionRing) (Object) this.levelProjectionMatrixBuffer).polydium$useRing();
    }
}
//?}
