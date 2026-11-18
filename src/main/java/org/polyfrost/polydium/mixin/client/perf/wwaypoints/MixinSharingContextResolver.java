package org.polyfrost.polydium.mixin.client.perf.wwaypoints;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.wwaypoints.client.sharing.SharingContextResolver", remap = false)
public abstract class MixinSharingContextResolver {
    /**
     * Same as matching {@code [A-Za-z0-9_]{3,16}}.
     *
     * @author Polydium
     * @reason regex per player per tick
     */
    @Overwrite
    private static boolean validUsername(String value) {
        if (value == null)
            return false;
        int length = value.length();
        if (length < 3 || length > 16)
            return false;
        for (int i = 0; i < length; i++) {
            if (!polydium$usernameChar(value.charAt(i)))
                return false;
        }
        return true;
    }

    @Unique
    private static boolean polydium$usernameChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
    }

    @WrapOperation(method = {"lambda$visiblePlayers$0", "lambda$visiblePlayers$1"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getString()Ljava/lang/String;", remap = true), require = 0)
    private static String polydium$usernameOrNull(Component component, Operation<String> original) {
        StringBuilder builder = new StringBuilder(16);
        Optional<?> stopped = component.visit(contents -> {
            if (builder.length() + contents.length() > 16)
                return FormattedText.STOP_ITERATION;
            for (int i = 0; i < contents.length(); i++) {
                if (!polydium$usernameChar(contents.charAt(i)))
                    return FormattedText.STOP_ITERATION;
            }
            builder.append(contents);
            return Optional.empty();
        });
        return stopped.isEmpty() && builder.length() >= 3 ? builder.toString() : null;
    }

    @WrapOperation(method = "visiblePlayers", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"), require = 0)
    private static void polydium$sortWithoutLowercasing(List<String> list, Comparator<? super String> comparator, Operation<Void> original) {
        original.call(list, String.CASE_INSENSITIVE_ORDER);
    }
}
