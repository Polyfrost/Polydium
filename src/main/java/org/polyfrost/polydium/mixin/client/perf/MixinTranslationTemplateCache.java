package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import org.polyfrost.polydium.client.perf.TranslationTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(TranslatableContents.class)
public abstract class MixinTranslationTemplateCache {
    @Shadow
    @Final
    private static FormattedText TEXT_PERCENT;

    @Shadow
    private FormattedText getArgument(int index) {
        throw new AssertionError();
    }

    @Inject(method = "decomposeTemplate", at = @At("HEAD"), cancellable = true)
    private void polydium$replayTemplate(String template, Consumer<FormattedText> decomposedParts, CallbackInfo ci) {
        Object[] parts = TranslationTemplate.parts(template, TEXT_PERCENT, FormattedText::of);
        if (parts == null) {
            return;
        }
        try {
            for (Object part : parts) {
                decomposedParts.accept(part instanceof Integer index ? this.getArgument(index) : (FormattedText) part);
            }
        } catch (IllegalArgumentException e) {
            throw new TranslatableFormatException((TranslatableContents) (Object) this, e);
        }
        ci.cancel();
    }
}
//?}
