//~ identifier
package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.client.perf.UnifontCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Mixin(UnihexProvider.Definition.class)
public abstract class MixinUnifontCache {
    @Shadow
    @Final
    private Identifier hexFile;

    @Shadow
    @Final
    private List<?> sizeOverrides;

    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/providers/UnihexProvider$Definition;loadData(Ljava/io/InputStream;)Lnet/minecraft/client/gui/font/providers/UnihexProvider;"))
    private UnihexProvider polydium$reuseParsedUnifont(UnihexProvider.Definition definition, InputStream zipFile, Operation<UnihexProvider> original) throws IOException {
        byte[] contents = zipFile.readAllBytes();
        byte[] digest = UnifontCache.digest(contents);
        UnihexProvider cached = UnifontCache.find(this.hexFile, this.sizeOverrides, digest);
        if (cached != null) {
            return cached;
        }
        UnihexProvider provider = original.call(definition, new ByteArrayInputStream(contents));
        UnifontCache.remember(this.hexFile, this.sizeOverrides, digest, provider);
        return provider;
    }
}
//?}
