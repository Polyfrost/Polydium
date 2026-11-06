package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.9 && < 26.1 {
/*import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * Mods that check whether a keybind is "physically down" (PolySprint, wWaypoints, ...) pass the bound key's
 * code straight to {@code InputConstants.isKeyDown}. For an unbound keybind that code is -1, so GLFW raises
 * GLFW_INVALID_ENUM and vanilla logs three error lines, every tick the mod polls. GLFW returns "released" for it
 * anyway, so answer that up front and skip the native call and the log spam.
 ^/
@Mixin(InputConstants.class)
public abstract class MixinUnboundKeyPoll {
    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    private static void polydium$unboundKeyIsUp(Window window, int key, CallbackInfoReturnable<Boolean> cir) {
        if (key < 0) {
            cir.setReturnValue(false);
        }
    }
}
*///?}
