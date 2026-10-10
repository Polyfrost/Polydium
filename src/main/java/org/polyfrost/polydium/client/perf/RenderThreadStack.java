package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import org.lwjgl.system.MemoryStack;

public final class RenderThreadStack {
    private static Owner owner;

    private RenderThreadStack() {
    }

    public static void capture(MemoryStack stack) {
        if (owner == null) {
            owner = new Owner(Thread.currentThread(), stack);
        }
    }

    public static Owner owner() {
        return owner;
    }

    public record Owner(Thread thread, MemoryStack stack) {
    }
}
//?}
