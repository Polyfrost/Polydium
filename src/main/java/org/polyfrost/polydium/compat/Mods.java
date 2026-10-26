package org.polyfrost.polydium.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class Mods {
    public static final boolean PARTICLE_CORE = loaded("particle_core");
    public static final boolean SERENE_SEASONS = loaded("sereneseasons");
    public static final boolean VULKANMOD = loaded("vulkanmod");
    public static final boolean DYNAMIC_LIGHTS = loaded("lambdynlights") || loaded("sodiumdynamiclights") || loaded("ryoamiclights");

    private Mods() {
    }

    public static boolean loaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
