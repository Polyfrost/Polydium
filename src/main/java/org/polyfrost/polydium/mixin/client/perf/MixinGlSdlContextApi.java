package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.mojang.renderpearl.backend.opengl.GlBackend;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.SharedLibrary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlBackend.class)
public abstract class MixinGlSdlContextApi {
    @Inject(method = "loadLibrary", at = @At("HEAD"))
    private void polydium$matchSdlVideoDriver(CallbackInfo ci) {
        if (!"x11".equals(SDLVideo.SDL_GetCurrentVideoDriver())
                || SDLHints.SDL_GetHintBoolean(SDLHints.SDL_HINT_VIDEO_FORCE_EGL, false)
                || Configuration.OPENGL_CONTEXT_API.get() != null) {
            return;
        }
        FunctionProvider provider = GL.getFunctionProvider();
        if (!(provider instanceof SharedLibrary library) || !library.getName().contains("EGL")) {
            return;
        }
        GL.destroy();
        Configuration.OPENGL_CONTEXT_API.set("native");
        try {
            GL.create();
        } catch (RuntimeException | LinkageError e) {
            Configuration.OPENGL_CONTEXT_API.set(null);
            GL.create();
        }
    }
}
//?}
