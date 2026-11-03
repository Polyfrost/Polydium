package org.polyfrost.polydium.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

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

    public static boolean loadedBefore(String id, String version) {
        try {
            Version fixed = Version.parse(version);
            return FabricLoader.getInstance().getModContainer(id).map(mod -> mod.getMetadata().getVersion().compareTo(fixed) < 0).orElse(false);
        } catch (VersionParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
