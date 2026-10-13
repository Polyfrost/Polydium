package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import org.polyfrost.polydium.client.perf.FontGeneration;
import org.polyfrost.polydium.client.perf.TextWidthCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public abstract class MixinTextWidthCache implements TextWidthCache.Access {
    @Unique
    private TextWidthCache polydium$widthCache;

    @Override
    public void polydium$enableWidthCache() {
        this.polydium$widthCache = new TextWidthCache(TextWidthCache.LIMIT);
    }

    @WrapOperation(method = "width(Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;stringWidth(Ljava/lang/String;)F"))
    private float polydium$cachedWidth(StringSplitter splitter, String text, Operation<Float> original) {
        TextWidthCache cache = this.polydium$widthCache;
        if (cache == null || text == null || !RenderSystem.isOnRenderThread()) {
            return original.call(splitter, text);
        }
        int generation = FontGeneration.get();
        Integer cached = cache.get(text, generation);
        if (cached != null) {
            return cached;
        }
        float width = original.call(splitter, text);
        cache.put(text, generation, Mth.ceil(width));
        return width;
    }
}
