package org.polyfrost.polydium.client.perf;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockStateCacheBatch {
    public static boolean collecting;
    private static List<BlockState> pending = new ArrayList<>();

    private BlockStateCacheBatch() {
    }

    public static void defer(BlockState state) {
        pending.add(state);
    }

    public static void buildAll() {
        collecting = false;
        List<BlockState> states = pending;

        pending = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        states.parallelStream().forEach(state -> {
            Thread.currentThread().setContextClassLoader(loader);
            state.initCache();
        });
    }
}
