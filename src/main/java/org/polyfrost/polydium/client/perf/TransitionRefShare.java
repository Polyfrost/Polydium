package org.polyfrost.polydium.client.perf;

//? if >= 1.21.2 {
import java.lang.ref.WeakReference;

public final class TransitionRefShare {
    public interface Holder {
        WeakReference<Object> polydium$transitionRef();

        void polydium$setTransitionRef(WeakReference<Object> ref);
    }

    private TransitionRefShare() {
    }

    public static WeakReference<Object> of(Object entity) {
        if (!(entity instanceof Holder holder))
            return new WeakReference<>(entity);
        WeakReference<Object> ref = holder.polydium$transitionRef();
        if (ref == null || ref.get() != entity) {
            ref = new WeakReference<>(entity);
            holder.polydium$setTransitionRef(ref);
        }
        return ref;
    }
}
//?}
