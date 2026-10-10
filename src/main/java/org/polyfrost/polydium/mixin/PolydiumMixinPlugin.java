package org.polyfrost.polydium.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PolydiumMixinPlugin implements IMixinConfigPlugin {
    private static final String SODIUM_PERF_PACKAGE = "org.polyfrost.polydium.mixin.client.perf.sodium.";
    //? if >= 26.1 {
    private static final String SODIUM_PERF_VERSION = "0.9.2";
    //?} else {
    /*private static final String SODIUM_PERF_VERSION = "0.8.14";
    *///?}

    private final boolean sodiumPerf = FabricLoader.getInstance().getModContainer("sodium")
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString().startsWith(SODIUM_PERF_VERSION))
            .orElse(false);

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return sodiumPerf || !mixinClassName.startsWith(SODIUM_PERF_PACKAGE);
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
