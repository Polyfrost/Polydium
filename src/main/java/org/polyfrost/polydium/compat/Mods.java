package org.polyfrost.polydium.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class Mods {
    public static final boolean IRIS = loaded("iris");
    public static final boolean OBE = loaded("optimisedblockentities") || loaded("obe");
    public static final boolean BBE = loaded("betterblockentities");
    public static final boolean IMMEDIATELY_FAST = loaded("immediatelyfast");
    public static final boolean ENTITY_CULLING = loaded("entityculling");
    public static final boolean NVIDIUM = loaded("nvidium");
    public static final boolean CONTINUITY = loaded("continuity");
    public static final boolean INDIUM = loaded("indium");
    public static final boolean BETTER_F3 = loaded("betterf3");
    public static final boolean CAXTON = loaded("caxton");
    public static final boolean MODERN_UI = loaded("modernui");
    public static final boolean PARTICLE_CORE = loaded("particle_core");
    public static final boolean PARTICLE_RAIN = loaded("particlerain");
    public static final boolean SERENE_SEASONS = loaded("sereneseasons");
    public static final boolean DYNAMIC_LIGHTS = loaded("lambdynlights") || loaded("sodiumdynamiclights") || loaded("ryoamiclights");
    public static final boolean DYNAMIC_FPS = loaded("dynamic_fps");
    public static final boolean IXERIS = loaded("ixeris");
    public static final boolean LITHIUM = loaded("lithium");
    public static final boolean FERRITE_CORE = loaded("ferritecore");
    public static final boolean FRAMED_BLOCKS = loaded("framedblocks");

    private Mods() {
    }

    public static boolean loaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
