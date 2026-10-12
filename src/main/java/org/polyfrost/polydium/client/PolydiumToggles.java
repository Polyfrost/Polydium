package org.polyfrost.polydium.client;

import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public class PolydiumToggles {
    //? if >= 1.21.11
    @Switch(title = "Deferred Sky", description = "Draws the sky after terrain so hidden sky pixels are skipped.")
    public static boolean deferredSky = true;
    //? if >= 1.21.8
    @Switch(title = "GUI Item Baking", description = "Redraws animated GUI items only when their animation actually moves.")
    public static boolean guiItemBake = true;
    //? if >= 1.21.11
    @Switch(title = "Particle Section Culling", description = "Skips particles in chunk sections Sodium has culled.")
    public static boolean particleSectionCull = true;
    //? if >= 1.21.11
    @Switch(title = "Larger Section Copy Cache", description = "Keeps more copied chunk sections for meshing. Uses more memory.")
    public static boolean sectionCacheSize = true;
    //? if >= 1.21.10
    @Switch(title = "Debug Entry Cache", description = "Refreshes F3 debug entries on an interval instead of every frame.")
    public static boolean debugEntryCache = true;
    //? if >= 1.21.11
    @Switch(title = "Sound Listener Throttle", description = "Stops sending unchanged listener position updates to the sound thread.")
    public static boolean soundListenerThrottle = true;
    //? if < 26.1
    //@Switch(title = "Precise Frame Limiter", description = "Replaces the vanilla FPS limiter with a more accurate one implemented in MC 26.1+.")
    public static boolean preciseFrameLimiter = true;
    //? if >= 1.21.11
    @Switch(title = "Hide Level Loading Screen", description = "Stops drawing the level loading screen once the world is loaded.")
    public static boolean hideLevelLoadingScreen = true;
    //? if >= 1.21.11
    @Switch(title = "Hide Reload Overlay")
    public static boolean hideReloadOverlay = true;
    //? if >= 1.21.11
    @Switch(title = "Hide Title Fade")
    public static boolean hideTitleFade = true;
}
