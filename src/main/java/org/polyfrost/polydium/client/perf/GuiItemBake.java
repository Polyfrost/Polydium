package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;

import java.util.ArrayList;

public final class GuiItemBake {
    public static final int SPRITE = 1;
    public static final int FOIL = 2;
    public static final int GLINT_INTERVAL = Integer.getInteger("polydium.guiGlintInterval", 0);

    public static long textureTick;

    private static final Stamps STAMPS = new Stamps();
    private static boolean preparing;
    private static long glintStep;

    private GuiItemBake() {
    }

    public interface Access {
        void polydium$bake(int reason);

        int polydium$baked();
    }

    public static void begin() {
        preparing = true;
        glintStep = GLINT_INTERVAL > 0 ? System.currentTimeMillis() / GLINT_INTERVAL : 0L;
    }

    public static void end() {
        preparing = false;
        STAMPS.commit();
    }

    public static boolean animated(ItemStackRenderState state, int reasons) {
        if (!preparing || state.isOversizedInGui() || !(state instanceof TrackingItemStackRenderState item)) {
            return true;
        }
        //? if >= 26.1 {
        return false;
        //?} else {
        /*return STAMPS.due(item.getModelIdentity(), stamp(reasons));
        *///?}
    }

    //? if >= 26.1 {
    public static boolean refresh(TrackingItemStackRenderState item) {
        int reasons = ((Access) item).polydium$baked();
        return preparing && reasons != 0 && !item.isAnimated() && STAMPS.due(item.getModelIdentity(), stamp(reasons));
    }
    //?}

    private static long stamp(int reasons) {
        return ((reasons & SPRITE) != 0 ? textureTick : 0L) * 1_000_003L + ((reasons & FOIL) != 0 ? glintStep : 0L);
    }

    public static final class Stamps {
        private static final int LIMIT = 4096;
        private final Object2LongOpenHashMap<Object> drawn = new Object2LongOpenHashMap<>();
        private final ArrayList<Object> dueKeys = new ArrayList<>();
        private final LongArrayList dueStamps = new LongArrayList();

        public Stamps() {
            this.drawn.defaultReturnValue(Long.MIN_VALUE);
        }

        public boolean due(Object key, long stamp) {
            if (this.drawn.getLong(key) == stamp) {
                return false;
            }
            this.dueKeys.add(key);
            this.dueStamps.add(stamp);
            return true;
        }

        public void commit() {
            if (this.drawn.size() > LIMIT) {
                this.drawn.clear();
            }
            for (int i = 0; i < this.dueKeys.size(); i++) {
                this.drawn.put(this.dueKeys.get(i), this.dueStamps.getLong(i));
            }
            this.dueKeys.clear();
            this.dueStamps.clear();
        }
    }
}
//?}
