package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.2 {
import java.lang.ref.WeakReference;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.TransitionRefShare;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class MixinTransitionRefHolder implements TransitionRefShare.Holder {
    @Unique
    private WeakReference<Object> polydium$transitionRef;

    @Override
    public WeakReference<Object> polydium$transitionRef() {
        return this.polydium$transitionRef;
    }

    @Override
    public void polydium$setTransitionRef(WeakReference<Object> ref) {
        this.polydium$transitionRef = ref;
    }
}
//?}
