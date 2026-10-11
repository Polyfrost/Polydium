package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.polyfrost.polydium.client.perf.GuiItemBake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.class)
public class MixinGuiItemBakeState implements GuiItemBake.Access {
    @Unique
    private int polydium$baked;

    @Override
    public void polydium$bake(int reason) {
        this.polydium$baked |= reason;
    }

    @Override
    public int polydium$baked() {
        return this.polydium$baked;
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void polydium$clearBaked(CallbackInfo ci) {
        this.polydium$baked = 0;
    }

    @ModifyReturnValue(method = "isAnimated", at = @At("RETURN"))
    private boolean polydium$bakedAnimated(boolean original) {
        return original || this.polydium$baked != 0 && GuiItemBake.animated((ItemStackRenderState) (Object) this, this.polydium$baked);
    }
}
//?}
