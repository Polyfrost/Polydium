package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 1.21.10 {
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
//?} else {
/*import net.minecraft.client.gui.font.glyphs.BakedGlyph;
*///?}
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.TextEffectRun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

//? if >= 1.21.8 {
@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
//?} else {
/*@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
*///?}
public abstract class MixinTextEffectMerge {
    //? if >= 1.21.10 {
    @Shadow private List<TextRenderable> effects;

    @Shadow protected abstract void markSize(float left, float top, float right, float bottom);

    @Unique private final TextEffectRun polydium$strikethrough = new TextEffectRun();
    @Unique private final TextEffectRun polydium$underline = new TextEffectRun();
    @Unique private TextEffectRun polydium$pending;

    @WrapOperation(
            method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/EffectGlyph;createEffect(FFFFFIIF)Lnet/minecraft/client/gui/font/TextRenderable;", ordinal = 0)
    )
    private TextRenderable polydium$extendStrikethrough(EffectGlyph glyph, float x0, float y0, float x1, float y1, float depth, int color, int shadowColor, float shadowOffset, Operation<TextRenderable> original) {
        return original.call(glyph, this.polydium$extend(this.polydium$strikethrough, x0, y0, x1, y1, depth, color, shadowColor, shadowOffset), y0, x1, y1, depth, color, shadowColor, shadowOffset);
    }

    @WrapOperation(
            method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/EffectGlyph;createEffect(FFFFFIIF)Lnet/minecraft/client/gui/font/TextRenderable;", ordinal = 1)
    )
    private TextRenderable polydium$extendUnderline(EffectGlyph glyph, float x0, float y0, float x1, float y1, float depth, int color, int shadowColor, float shadowOffset, Operation<TextRenderable> original) {
        return original.call(glyph, this.polydium$extend(this.polydium$underline, x0, y0, x1, y1, depth, color, shadowColor, shadowOffset), y0, x1, y1, depth, color, shadowColor, shadowOffset);
    }

    @Unique
    private float polydium$extend(TextEffectRun run, float x0, float y0, float x1, float y1, float depth, int color, int shadowColor, float shadowOffset) {
        this.polydium$pending = run;
        if (PolydiumToggles.textEffectMerge && run.continues(x0, y0, y1, depth, color, shadowColor, shadowOffset)) {
            run.x1 = x1;
            run.extended = true;
            return run.x0;
        }
        run.start(x0, y0, x1, y1, depth, color, shadowColor, shadowOffset);
        return x0;
    }

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    private void polydium$replaceExtendedEffect(TextRenderable effect, CallbackInfo ci) {
        TextEffectRun run = this.polydium$pending;
        if (run == null) {
            return;
        }
        this.polydium$pending = null;
        if (run.extended) {
            this.effects.set(run.index, effect);
            this.markSize(effect.left(), effect.top(), effect.right(), effect.bottom());
            ci.cancel();
        } else {
            run.index = this.effects == null ? 0 : this.effects.size();
        }
    }
    //?} else if >= 1.21.4 {
    /*@Shadow private List<BakedGlyph.Effect> effects;

    @ModifyVariable(method = "addEffect", at = @At("HEAD"), argsOnly = true)
    private BakedGlyph.Effect polydium$extendPreviousEffect(BakedGlyph.Effect effect) {
        List<BakedGlyph.Effect> effects = this.effects;
        if (effects == null || !PolydiumToggles.textEffectMerge) {
            return effect;
        }
        int size = effects.size();
        for (int index = size - 1; index >= 0 && index >= size - 2; index--) {
            BakedGlyph.Effect previous = effects.get(index);
            if (previous.x1() == effect.x0() && previous.y0() == effect.y0() && previous.y1() == effect.y1() && previous.depth() == effect.depth()
                    && previous.color() == effect.color() && previous.shadowColor() == effect.shadowColor() && previous.shadowOffset() == effect.shadowOffset()) {
                effects.remove(index);
                return new BakedGlyph.Effect(previous.x0(), effect.y0(), effect.x1(), effect.y1(), effect.depth(), effect.color(), effect.shadowColor(), effect.shadowOffset());
            }
        }
        return effect;
    }
    *///?} else {
    /*@Shadow private List<BakedGlyph.Effect> effects;

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    private void polydium$extendPreviousEffect(BakedGlyph.Effect effect, CallbackInfo ci) {
        List<BakedGlyph.Effect> effects = this.effects;
        if (effects == null || !PolydiumToggles.textEffectMerge) {
            return;
        }
        BakedGlyphEffectAccessor next = (BakedGlyphEffectAccessor) (Object) effect;
        if (next.polydium$a() != 1.0F) {
            return;
        }
        int size = effects.size();
        for (int index = size - 1; index >= 0 && index >= size - 2; index--) {
            BakedGlyphEffectAccessor previous = (BakedGlyphEffectAccessor) (Object) effects.get(index);
            if (previous.polydium$x1() - 1.0F == next.polydium$x0() && previous.polydium$y0() == next.polydium$y0() && previous.polydium$y1() == next.polydium$y1()
                    && previous.polydium$depth() == next.polydium$depth() && previous.polydium$r() == next.polydium$r() && previous.polydium$g() == next.polydium$g()
                    && previous.polydium$b() == next.polydium$b() && previous.polydium$a() == 1.0F) {
                effects.set(index, new BakedGlyph.Effect(previous.polydium$x0(), next.polydium$y0(), next.polydium$x1(), next.polydium$y1(), next.polydium$depth(),
                        next.polydium$r(), next.polydium$g(), next.polydium$b(), 1.0F));
                ci.cancel();
                return;
            }
        }
    }
    *///?}
}
