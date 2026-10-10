package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontOption;
import org.polyfrost.polydium.client.perf.FontSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FontManager.class)
public abstract class MixinFontSelectionManager {
    @Shadow
    private static Set<FontOption> getFontOptions(Options options) {
        throw new AssertionError();
    }

    @ModifyReturnValue(method = "prepare", at = @At("RETURN"))
    private CompletableFuture<?> polydium$selectProvidersOffThread(CompletableFuture<?> preparation, @Local(argsOnly = true) Executor executor) {
        if (!FontSelection.AVAILABLE) {
            return preparation;
        }
        return preparation.thenApplyAsync(prepared -> {
            FontSelection.prepare(((FontPreparationAccessor) prepared).polydium$fontSets(), getFontOptions(Minecraft.getInstance().options));
            return prepared;
        }, executor);
    }

    @Inject(method = "apply", at = @At("TAIL"))
    private void polydium$dropPreparedSelection(CallbackInfo ci) {
        FontSelection.clear();
    }

    @WrapMethod(method = "finalizeProviderLoading")
    private void polydium$skipLookupOnlyWarmup(List<GlyphProvider.Conditional> list, GlyphProvider.Conditional fallback, Operation<Void> original) {
        if (!FontSelection.AVAILABLE || fallback.provider().getClass() != AllMissingGlyphProvider.class || !FontSelection.onlyLookupProviders(list)) {
            original.call(list, fallback);
            return;
        }
        list.add(0, fallback);
    }
}
//?}
