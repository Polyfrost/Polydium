package org.polyfrost.polydium.mixin.client.perf.gpuprofile;

//? if = 1.21.4 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgram;
import org.polyfrost.polydium.client.perf.gpuprofile.GpuProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public abstract class MixinGpuProfileLegacyShader {
    @Inject(method = "setShader(Lnet/minecraft/client/renderer/ShaderProgram;)Lnet/minecraft/client/renderer/CompiledShaderProgram;", at = @At("RETURN"))
    private static void polydium$profileShader(ShaderProgram program, CallbackInfoReturnable<CompiledShaderProgram> cir) {
        if (GpuProfiler.recording)
            GpuProfiler.shaderName(cir.getReturnValue(), "shader " + program.configId());
    }
}
*///?}
