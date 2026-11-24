package org.polyfrost.polydium.mixin.client.perf.shieldstatus;

//? if >= 1.21.8 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = GuiRenderer.class, priority = 1500)
public class MixinShieldStatusIdentityCheck {
    @Unique
    private static final String POLYDIUM$MIXIN = "walksy.shieldstatus.mixin.GuiRendererMixin";

    @Unique
    private static final int POLYDIUM$MAX_CACHED = 512;

    @Unique
    private static final Map<Object, String> polydium$identityText = new HashMap<>();

    @TargetHandler(mixin = POLYDIUM$MIXIN, name = "prepareItemElements")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/lang/Object;toString()Ljava/lang/String;"), require = 0, expect = 0)
    private String polydium$cachedShieldCheck(Object identity, Operation<String> original) {
        if (!(identity instanceof List<?> elements)) return original.call(identity);
        String cached = polydium$identityText.get(elements);
        if (cached != null) return cached;
        String text = original.call(identity).contains("shield") ? "shield" : "";
        if (polydium$identityText.size() >= POLYDIUM$MAX_CACHED) polydium$identityText.clear();
        polydium$identityText.put(new ArrayList<>(elements), text);
        return text;
    }
}
//?}
