package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.polyfrost.polydium.client.perf.FontGeneration;
import org.polyfrost.polydium.client.perf.PreparedTextCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Font.class)
public abstract class MixinPreparedTextCache implements PreparedTextCache.Access {
    @Unique private static final int POLYDIUM_SHADOW = 1;
    @Unique private static final int POLYDIUM_INCLUDE_EMPTY = 2;
    @Unique private static final int POLYDIUM_STRING = 4;
    @Unique private static final int POLYDIUM_OUTLINE = 8;

    @Unique
    private PreparedTextCache<Font.PreparedText> polydium$preparedTextCache;

    @Unique
    private FormattedCharSink polydium$preparedTextCapture;

    @Override
    public void polydium$enablePreparedTextCache() {
        PreparedTextCache<Font.PreparedText> cache = new PreparedTextCache<>(PreparedTextCache.LIMIT);
        this.polydium$preparedTextCache = cache;
        this.polydium$preparedTextCapture = (position, style, codePoint) -> PreparedTextCache.cacheable(style) && cache.add(position, style, codePoint);
    }

    //? if >= 1.21.11 {
    @WrapMethod(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;")
    private Font.PreparedText polydium$cachedSequence(FormattedCharSequence text, float x, float y, int color, boolean shadow, boolean includeEmpty, int background, Operation<Font.PreparedText> original) {
    //?} else {
    /*@WrapMethod(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;")
    private Font.PreparedText polydium$cachedSequence(FormattedCharSequence text, float x, float y, int color, boolean shadow, int background, Operation<Font.PreparedText> original) {
        boolean includeEmpty = false;
    *///?}
        PreparedTextCache<Font.PreparedText> cache = this.polydium$preparedTextCache;
        PreparedTextCache.Key key = null;
        if (cache != null && RenderSystem.isOnRenderThread()) {
            cache.begin(FontGeneration.get());
            int flags = (shadow ? POLYDIUM_SHADOW : 0) | (includeEmpty ? POLYDIUM_INCLUDE_EMPTY : 0);
            Font.PreparedText seen = cache.seen(text, x, y, color, background, flags);
            if (seen != null) {
                return seen;
            }
            if (text.accept(this.polydium$preparedTextCapture)) {
                Font.PreparedText cached = cache.get(x, y, color, background, flags);
                if (cached != null) {
                    return cache.remember(cached);
                }
                key = cache.miss();
                text = polydium$replay(key);
            }
        }
        Font.PreparedText prepared = original.call(text, x, y, color, shadow, /*? if >= 1.21.11 {*/includeEmpty, /*?}*/background);
        if (key != null) {
            cache.put(key, cache.remember(prepared));
        }
        return prepared;
    }

    @WrapMethod(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;")
    private Font.PreparedText polydium$cachedString(String text, float x, float y, int color, boolean shadow, int background, Operation<Font.PreparedText> original) {
        PreparedTextCache<Font.PreparedText> cache = this.polydium$preparedTextCache;
        PreparedTextCache.Key key = null;
        if (cache != null && text != null && RenderSystem.isOnRenderThread() && !Language.getInstance().isDefaultRightToLeft()) {
            cache.begin(FontGeneration.get());
            if (StringDecomposer.iterateFormatted(text, Style.EMPTY, this.polydium$preparedTextCapture)) {
                Font.PreparedText cached = cache.get(x, y, color, background, (shadow ? POLYDIUM_SHADOW : 0) | POLYDIUM_STRING);
                if (cached != null) {
                    return cached;
                }
                key = cache.miss();
            }
        }
        Font.PreparedText prepared = original.call(text, x, y, color, shadow, background);
        if (key != null) {
            cache.put(key, prepared);
        }
        return prepared;
    }

    //? if >= 26.2 {
    @WrapMethod(method = "prepare8xTextOutline")
    private Font.PreparedText polydium$cachedOutline(FormattedCharSequence text, float x, float y, int outlineColor, Operation<Font.PreparedText> original) {
        PreparedTextCache<Font.PreparedText> cache = this.polydium$preparedTextCache;
        PreparedTextCache.Key key = null;
        if (cache != null && RenderSystem.isOnRenderThread()) {
            cache.begin(FontGeneration.get());
            Font.PreparedText seen = cache.seen(text, x, y, outlineColor, 0, POLYDIUM_OUTLINE);
            if (seen != null) {
                return seen;
            }
            if (text.accept(this.polydium$preparedTextCapture)) {
                Font.PreparedText cached = cache.get(x, y, outlineColor, 0, POLYDIUM_OUTLINE);
                if (cached != null) {
                    return cache.remember(cached);
                }
                key = cache.miss();
                text = polydium$replay(key);
            }
        }
        Font.PreparedText prepared = original.call(text, x, y, outlineColor);
        if (key != null) {
            cache.put(key, cache.remember(prepared));
        }
        return prepared;
    }
    //?}

    @Unique
    private static FormattedCharSequence polydium$replay(PreparedTextCache.Key key) {
        return sink -> {
            for (int i = 0, length = key.length(); i < length; i++) {
                if (!sink.accept(key.position(i), (Style) key.style(i), key.codePoint(i))) {
                    return false;
                }
            }
            return true;
        };
    }
}
//?}
