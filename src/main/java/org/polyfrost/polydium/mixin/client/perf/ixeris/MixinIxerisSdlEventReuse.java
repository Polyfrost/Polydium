package org.polyfrost.polydium.mixin.client.perf.ixeris;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "me.decce.ixeris.core.sdl.SdlEventQueue", remap = false)
public abstract class MixinIxerisSdlEventReuse {
    @Unique
    private SDL_Event polydium$spare;

    @WrapOperation(method = "pollEvent", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDL_Event;malloc()Lorg/lwjgl/sdl/SDL_Event;"), remap = false, require = 0)
    private SDL_Event polydium$takeSpare(Operation<SDL_Event> original) {
        SDL_Event spare = this.polydium$spare;
        if (spare != null) {
            this.polydium$spare = null;
            return spare;
        }
        return original.call();
    }

    @WrapWithCondition(method = "pollEvent", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDL_Event;free()V"), remap = false, require = 0)
    private boolean polydium$keepSpare(SDL_Event event) {
        if (this.polydium$spare == null) {
            this.polydium$spare = event;
            return false;
        }
        return true;
    }
}
//?}
