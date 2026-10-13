package org.polyfrost.polydium.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import org.polyfrost.polydium.compat.Mods;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolydiumMixinPlugin implements IMixinConfigPlugin {
    private static final String SODIUM_PERF_PACKAGE = "org.polyfrost.polydium.mixin.client.perf.sodium.";
    //? if >= 26.3 {
    private static final String[] SODIUM_PERF_VERSIONS = {"0.9.2", "0.9.3"};
    //?} else if >= 26.1 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.9.2"};
    *///?} else if >= 1.21.11 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.8.14", "0.8.15"};
    *///?} else if >= 1.21.8 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.7.3"};
    *///?} else if >= 1.21.4 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.6.13"};
    *///?} else {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.8.13"};
    *///?}

    public static final boolean SODIUM_PERF = FabricLoader.getInstance().getModContainer("sodium")
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
            .map(version -> Arrays.stream(SODIUM_PERF_VERSIONS).anyMatch(version::startsWith))
            .orElse(false);

    private static final String[] DISABLED = System.getProperty("polydium.disable", "").split(",");

    private static final String[] ENABLED = System.getProperty("polydium.enable", "").split(",");

    private static final Map<String, String[]> STAND_DOWN = Map.ofEntries(
            Map.entry("TextWidthCache", new String[]{"modernui"}),
            Map.entry("LtrTextFastPath", new String[]{"modernui"}),
            Map.entry("PreparedText", new String[]{"caxton"}),
            Map.entry("PreparedLanguage", new String[]{"languagereload"}),
            Map.entry("SinglePassTextShadow", new String[]{"caxton"})
    );

    private static final String[] OPT_IN = {"UnfocusedFullscreenThrottle"};

    public static boolean isEnabled(String fragment) {
        return contains(ENABLED, fragment) && !isDisabled(fragment);
    }

    private static boolean contains(String[] fragments, String name) {
        for (String fragment : fragments) {
            if (!fragment.isEmpty() && (name.contains(fragment) || fragment.contains(name)))
                return true;
        }

        return false;
    }

    private static boolean standsDown(String mixinClassName) {
        for (Map.Entry<String, String[]> entry : STAND_DOWN.entrySet()) {
            if (!mixinClassName.contains(entry.getKey()))
                continue;

            for (String mod : entry.getValue()) {
                if (Mods.loaded(mod)) {
                    LoggerFactory.getLogger("polydium/perf").debug("{} stands down: {} is loaded", mixinClassName, mod);
                    return true;
                }
            }
        }

        return false;
    }

    private static final String[] GL_BUFFER_UNIT = {
            "org.polyfrost.polydium.mixin.client.perf.opengl.MixinGlBufferMapFlags",
            "org.polyfrost.polydium.mixin.client.perf.opengl.MixinGlBufferNoUnmapFlush",
            "org.polyfrost.polydium.mixin.client.perf.opengl.MixinGlBufferStorageCoherent"
    };

    public static boolean isDisabled(String mixinClassName) {
        if (contains(DISABLED, mixinClassName))
            return true;

        for (String member : GL_BUFFER_UNIT) {
            if (!member.equals(mixinClassName))
                continue;

            for (String other : GL_BUFFER_UNIT) {
                if (contains(DISABLED, other))
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isDisabled(mixinClassName))
            return false;

        if (standsDown(mixinClassName))
            return false;

        if (contains(OPT_IN, mixinClassName) && !contains(ENABLED, mixinClassName))
            return false;

        return SODIUM_PERF || !mixinClassName.startsWith(SODIUM_PERF_PACKAGE);
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
