package org.polyfrost.polydium.client.perf;

//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.RenderPass;

public final class MainPassSwap {

    private static RenderPass reopened;

    private MainPassSwap() {
    }

    public static void reopened(RenderPass pass) {
        if (reopened != null)
            reopened.close();

        reopened = pass;
    }

    public static RenderPass take() {
        RenderPass pass = reopened;

        reopened = null;

        return pass;
    }
}
//?}
