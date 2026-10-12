package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.language.FormattedBidiReorder;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.SubStringSource;
import net.minecraft.util.FormattedCharSequence;
import org.polyfrost.polydium.client.perf.LtrText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FormattedBidiReorder.class)
public abstract class MixinLtrTextFastPath {
    @Inject(
            method = "reorder",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/network/chat/SubStringSource;create(Lnet/minecraft/network/chat/FormattedText;Lit/unimi/dsi/fastutil/ints/Int2IntFunction;Ljava/util/function/UnaryOperator;)Lnet/minecraft/network/chat/SubStringSource;"
            ),
            cancellable = true
    )
    private static void polydium$singleLtrRun(FormattedText text, boolean defaultRightToLeft, CallbackInfoReturnable<FormattedCharSequence> cir, @Local SubStringSource source) {
        if (defaultRightToLeft) {
            return;
        }
        String plainText = source.getPlainText();
        if (LtrText.isSingleLtrRun(plainText)) {
            cir.setReturnValue(FormattedCharSequence.composite(source.substring(0, plainText.length(), false)));
        }
    }
}
//?}
