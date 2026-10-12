package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.fabricmc.loader.api.FabricLoader;

public final class AudioDeviceReuse {
    public static final boolean AVAILABLE = !FabricLoader.getInstance().isModLoaded("sound_physics_remastered")
            && !FabricLoader.getInstance().isModLoaded("soundphysics");

    public static boolean reloading;

    private AudioDeviceReuse() {
    }
}
//?}
