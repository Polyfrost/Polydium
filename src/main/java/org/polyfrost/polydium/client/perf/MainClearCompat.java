package org.polyfrost.polydium.client.perf;

//? if >= 1.21.5 {
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;

public final class MainClearCompat {
    private static final boolean IRIS = FabricLoader.getInstance().isModLoaded("iris");

    private MainClearCompat() {
    }

    public static boolean worldClearReplaced() {
        return IRIS && Iris.packInUse();
    }

    private static final class Iris {
        static boolean packInUse() {
            return IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
//?}
