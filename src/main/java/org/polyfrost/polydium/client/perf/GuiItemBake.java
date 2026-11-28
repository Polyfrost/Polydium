package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
//? if >= 26.1
import net.minecraft.client.gui.render.DynamicAtlasAllocator;

public final class GuiItemBake {
    public static final int SPRITE = 1;
    /** Foil baked into the slot, redrawn every {@link #GLINT_INTERVAL} ms. */
    public static final int FOIL = 2;
    public static final int GLINT_INTERVAL = Integer.getInteger("polydium.guiGlintInterval", 0);

    public static long textureTick;

    private static boolean preparing;
    private static long glintStep;

    private GuiItemBake() {
    }

    public interface Access {
        void polydium$bake(int reason);

        int polydium$baked();
    }

    public interface Slot {
        long polydium$stamp();

        void polydium$setStamp(long stamp);
    }

    public static void begin() {
        preparing = true;
        glintStep = GLINT_INTERVAL > 0 ? System.currentTimeMillis() / GLINT_INTERVAL : 0L;
    }

    public static void end() {
        preparing = false;
    }

    public static boolean animated(ItemStackRenderState state) {
        return !preparing || state.isOversizedInGui() || !(state instanceof TrackingItemStackRenderState);
    }

    //? if >= 26.1 {
    public static DynamicAtlasAllocator.SlotState refresh(DynamicAtlasAllocator.SlotState state, DynamicAtlasAllocator.Slot slot, TrackingItemStackRenderState item) {
        long stamp = stamp(item);
        if (stamp == 0L) {
            return state;
        }
        Slot stamped = (Slot) slot;
        boolean ready = state == DynamicAtlasAllocator.SlotState.READY;
        if (ready && stamped.polydium$stamp() == stamp) {
            return state;
        }
        stamped.polydium$setStamp(stamp);
        return ready ? DynamicAtlasAllocator.SlotState.STALE : state;
    }
    //?} else {
    /*public static boolean redraw(TrackingItemStackRenderState item, Object slot) {
        long stamp = stamp(item);
        if (stamp == 0L || slot == null) {
            return false;
        }
        Slot stamped = (Slot) slot;
        if (stamped.polydium$stamp() == stamp) {
            return false;
        }
        stamped.polydium$setStamp(stamp);
        return true;
    }

    public static Object stampNewSlot(Object slot, TrackingItemStackRenderState item) {
        ((Slot) slot).polydium$setStamp(stamp(item));
        return slot;
    }
    *///?}

    // 0 if Polydium doesn't cache this item
    private static long stamp(TrackingItemStackRenderState item) {
        int reasons = ((Access) item).polydium$baked();
        if (!preparing || reasons == 0 || item.isAnimated()) {
            return 0L;
        }
        long time = ((reasons & SPRITE) != 0 ? textureTick : 0L) + ((reasons & FOIL) != 0 ? glintStep : 0L);
        return time << 3 | reasons;
    }
}
//?}
