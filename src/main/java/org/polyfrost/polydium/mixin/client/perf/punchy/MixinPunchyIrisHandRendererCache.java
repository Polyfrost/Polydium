package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.fabricmc.loader.api.FabricLoader;
//? if >= 26.3 {
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
//?} else {
/*import net.minecraft.client.renderer.ItemInHandRenderer;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//? if >= 26.3 {
@Mixin(value = FirstPersonHandsAndItemsRenderer.class, priority = 1100)
//?} else {
/*@Mixin(value = ItemInHandRenderer.class, priority = 1100)
*///?}
public class MixinPunchyIrisHandRendererCache {
    @Unique
    private static final boolean POLYDIUM$IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    @WrapMethod(method = "punchy$resolveIrisHandRendererReflection", remap = false, require = 0)
    private static boolean polydium$skipWithoutIris(Operation<Boolean> original) {
        return POLYDIUM$IRIS_LOADED && original.call();
    }
}
