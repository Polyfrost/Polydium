package org.polyfrost.polydium.client;

import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public class PolydiumToggles {
    //? if >= 1.21.10
    @Switch(title = "Deferred Sky", description = "Draws the sky after terrain so hidden sky pixels are skipped.")
    public static boolean deferredSky = true;
    //? if >= 1.21.8
    @Switch(title = "GUI Item Baking", description = "Redraws animated GUI items only when their animation actually moves.")
    public static boolean guiItemBake = true;
    @Switch(title = "Particle Section Culling", description = "Skips particles in chunk sections Sodium has culled.")
    public static boolean particleSectionCull = true;
    @Switch(title = "Larger Section Copy Cache", description = "Keeps more copied chunk sections for meshing. Uses more memory.")
    public static boolean sectionCacheSize = true;
    //? if >= 1.21.10
    @Switch(title = "Debug Entry Cache", description = "Refreshes F3 debug entries on an interval instead of every frame.")
    public static boolean debugEntryCache = true;
    @Switch(title = "Sound Listener Throttle", description = "Stops sending unchanged listener position updates to the sound thread.")
    public static boolean soundListenerThrottle = true;
    //? if < 26.1
    //@Switch(title = "Precise Frame Limiter", description = "Replaces the vanilla FPS limiter with a more accurate one implemented in MC 26.1+.")
    public static boolean preciseFrameLimiter = true;
    @Switch(title = "Hide Level Loading Screen", description = "Stops drawing the level loading screen once the world is loaded.")
    public static boolean hideLevelLoadingScreen = true;
    @Switch(title = "Hide Reload Overlay")
    public static boolean hideReloadOverlay = true;
    @Switch(title = "Hide Title Fade")
    public static boolean hideTitleFade = true;
    @Switch(title = "Free Sprite Mipmaps", description = "Frees generated sprite mipmaps from the CPU once atlas is uploaded. Applies on next resource reload.")
    public static boolean spriteMipRelease = true;
    @Switch(title = "Merge Text Decorations", description = "Draws each underlined or strikethrough run of text as one quad instead of one per character.")
    public static boolean textEffectMerge = true;
    //? if < 1.21.4
    //@Switch(title = "Single-Pass Text Shadows", description = "Draws text shadows in the same pass as the text, as MC 1.21.4+ does.")
    public static boolean singlePassTextShadow = true;
    //? if >= 26.3
    @Switch(title = "Sky In Main Pass", description = "Draws the deferred sky inside the main render pass instead of closing and reopening it.")
    public static boolean skyMainPass = true;
    @Switch(title = "Entity Outline Scissor", description = "Runs the glowing outline post passes only over the screen area the outlines can reach.")
    public static boolean outlineScissor = true;
    @Switch(title = "Idle Block Entity Ticks", description = "Skips client ticks of chests with resting lids and of spawners with no player nearby.")
    public static boolean idleBlockEntityTicks = true;
    //? if >= 1.21.11
    @Switch(title = "Early Swapchain Acquire", description = "With vsync off on Vulkan, takes the next screen image right after the GPU wait instead of after the client tick.")
    public static boolean earlyAcquire = true;
    @Switch(title = "Lock-free Dynamic Light Reads", description = "Lets chunk meshing read LambDynamicLights' light sources without taking its shared lock for every block, falling back to the lock only when the sources changed during the read.")
    public static boolean dynamicLightOptimisticRead = true;
    @Switch(title = "Spread Light Updates", description = "Spreads ScalableLux's client light updates over a few frames instead of doing a whole burst of block changes in one frame, and keeps them out of frames that also ran a game tick.")
    public static boolean clientLightBudget = true;
    @Switch(title = "Mark Loaded Chunks Dirty Once", description = "Stops ScalableLux from marking a newly loaded chunk's sections for a rebuild several times over; the chunk is still marked once, as in vanilla.")
    public static boolean chunkLoadDirtyOnce = true;
    @Switch(title = "Lock-Free Particle Light Cache", description = "Reads Particle Core's shared particle light cache without locking it, so particles at the same block on different threads no longer wait for each other's light lookup.")
    public static boolean particleLightCacheRead = true;
    //? if >= 1.21.10
    @Switch(title = "Overlapped Particle Ticking", description = "Runs Particle Core's particle tick on its own high-priority threads and lets it keep running while the frame's other rendering work starts, collecting particles for drawing only when they are submitted.")
    public static boolean particleTickOverlap = true;
    @Switch(title = "Synced Data Copies", description = "Reads entity flags, custom names and display view ranges from plain copies instead of the synced entity data table.")
    public static boolean syncedDataCopies = true;
    @Switch(title = "Parallel Block State Caches", description = "Builds the shape and light caches of all block states on all cores at startup.")
    public static boolean parallelBlockStateCache = true;
    @Switch(title = "No Profile Wait Before Title", description = "Builds the screens shown before the title screen without waiting for the session server profile.")
    public static boolean initialScreensProfile = true;
    //? if >= 1.21.11
    @Switch(title = "Animated Sprite Order", description = "Draws interpolated animated textures before frame-switching ones so each atlas pass binds each pipeline once. Applies from the next resource reload.")
    public static boolean animatedSpriteOrder = true;
    //? if >= 26.2
    @Switch(title = "Nearest Plane Quad Order", description = "With Sodium, draws each terrain face direction's quads nearest plane first so the GPU shades fewer hidden faces. Applies to sections built after the change.")
    public static boolean nearestPlaneQuadOrder = true;
    //? if >= 26.2
    @Switch(title = "Background Vertex Copy", description = "On Vulkan, copies the world's entity, text, item and particle vertices into GPU memory on a separate thread while terrain is prepared.")
    public static boolean backgroundVertexCopy = true;
    @Switch(title = "Xaero's Minimap HUD Caching", description = "Caches Xaero's Minimap and redraws it at most 60 times per second.")
    public static boolean xaeroMinimapHudCache = true;
}
