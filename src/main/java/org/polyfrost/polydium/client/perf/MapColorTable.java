package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.world.level.material.MapColor;

public final class MapColorTable {
    private static final int[] COLORS = new int[256];

    static {
        for (int packedId = 0; packedId < 256; packedId++) {
            COLORS[packedId] = MapColor.getColorFromPackedId(packedId);
        }
    }

    private MapColorTable() {
    }

    public static int get(int packedId) {
        return COLORS[packedId & 0xFF];
    }
}
//?}
