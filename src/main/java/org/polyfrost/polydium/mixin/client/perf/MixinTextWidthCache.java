package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import org.polyfrost.polydium.client.perf.FontGeneration;
import org.polyfrost.polydium.client.perf.TextWidthCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Font.class)
public abstract class MixinTextWidthCache implements TextWidthCache.Access {
    @Unique
    private TextWidthCache polydium$widthCache;

    @Override
    public void polydium$enableWidthCache() {
        this.polydium$widthCache = new TextWidthCache(TextWidthCache.LIMIT);
    }

    @WrapMethod(method = "width(Ljava/lang/String;)I")
    private int polydium$cachedWidth(String text, Operation<Integer> original) {
        TextWidthCache cache = this.polydium$widthCache;
        if (cache == null || text == null || !RenderSystem.isOnRenderThread()) {
            return original.call(text);
        }
        int generation = FontGeneration.get();
        Integer cached = cache.get(text, generation);
        if (cached != null) {
            return cached;
        }
        int width = original.call(text);
        cache.put(text, generation, width);
        return width;
    }
}
