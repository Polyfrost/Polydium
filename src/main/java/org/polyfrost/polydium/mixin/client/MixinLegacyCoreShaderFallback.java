package org.polyfrost.polydium.mixin.client;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.polyfrost.polydium.client.LegacyCoreShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(ShaderManager.class)
public abstract class MixinLegacyCoreShaderFallback {
    @ModifyExpressionValue(
        method = "loadConfigs",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Lnet/minecraft/server/packs/resources/ResourceManager$Selector;)Ljava/util/Map;")
    )
    private static Map<Identifier, Resource> polydium$skipLegacyShaders(Map<Identifier, Resource> files, @Local(argsOnly = true) ResourceManager manager) {
        return LegacyCoreShaders.replaceLegacy(files, manager);
    }
}
//?}
