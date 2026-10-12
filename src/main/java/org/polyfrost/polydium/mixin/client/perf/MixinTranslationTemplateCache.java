package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import org.polyfrost.polydium.client.perf.TranslationTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

    @WrapMethod(method = "decomposeTemplate")
    private void polydium$replayTemplate(String template, Consumer<FormattedText> decomposedParts, Operation<Void> original) {
        Object[] parts = TranslationTemplate.parts(template, TEXT_PERCENT, FormattedText::of);
        if (parts == null) {
            original.call(template, decomposedParts);
            return;
        }
        try {
            for (Object part : parts) {
                decomposedParts.accept(part instanceof Integer index ? this.getArgument(index) : (FormattedText) part);
            }
        } catch (IllegalArgumentException e) {
            throw new TranslatableFormatException((TranslatableContents) (Object) this, e);
        }
    }
}
//?}
