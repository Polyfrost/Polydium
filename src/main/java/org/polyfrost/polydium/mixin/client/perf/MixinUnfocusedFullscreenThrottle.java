package org.polyfrost.polydium.mixin.client.perf;


//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FramerateLimitTracker.class)
public abstract class MixinUnfocusedFullscreenThrottle {

    @WrapOperation(method = "getThrottleReason", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isIconified()Z"))
    private boolean polydium$unfocusedExclusiveFullscreen(Window window, Operation<Boolean> original) {
        return original.call(window) || !window.isFocused() && window.isFullscreen() && ((WindowExclusiveFullscreenAccessor) (Object) window).polydium$exclusiveFullscreen();
    }
}
*///?} else if >= 26.1 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FramerateLimitTracker.class)
public abstract class MixinUnfocusedFullscreenThrottle {

    @WrapOperation(method = "getThrottleReason", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isIconified()Z"))
    private boolean polydium$unfocusedExclusiveFullscreen(Window window, Operation<Boolean> original) {
        return original.call(window) || !window.isFocused() && window.isFullscreen();
    }
}
*///?} else if >= 1.21.5 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FramerateLimitTracker.class)
public abstract class MixinUnfocusedFullscreenThrottle {

    @WrapOperation(method = "getThrottleReason", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isIconified()Z"))
    private boolean polydium$unfocusedExclusiveFullscreen(Window window, Operation<Boolean> original) {
        return original.call(window) || !Minecraft.getInstance().isWindowActive() && window.isFullscreen();
    }
}
*///?} else if >= 1.21.4 && < 1.21.5 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FramerateLimitTracker.class)
public abstract class MixinUnfocusedFullscreenThrottle {

    @WrapOperation(method = "getFramerateLimit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isIconified()Z"))
    private boolean polydium$unfocusedExclusiveFullscreen(Window window, Operation<Boolean> original) {
        return original.call(window) || !Minecraft.getInstance().isWindowActive() && window.isFullscreen();
    }
}
*///?}
