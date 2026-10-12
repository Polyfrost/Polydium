package org.polyfrost.polydium.client.perf;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;

public final class IrisShaders {
    private static final boolean IRIS = FabricLoader.getInstance().isModLoaded("iris");

    private IrisShaders() {
    }

    public static boolean packInUse() {
        return IRIS && Api.packInUse();
    }

    private static final class Api {
        static boolean packInUse() {
            return IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
