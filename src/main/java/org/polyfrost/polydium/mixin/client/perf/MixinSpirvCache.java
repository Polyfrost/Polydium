package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
//? if >= 26.3 {
import com.mojang.renderpearl.backend.api.SpvModule;
import com.mojang.renderpearl.frontend.shaders.SPIRVModule;
//?} else {
/*import com.mojang.blaze3d.vulkan.glsl.IntermediaryShaderModule;
import com.mojang.renderpearl.util.ShaderCompileException;
import org.lwjgl.system.MemoryUtil;
*///?}
import com.mojang.renderpearl.frontend.shaders.GlslCompiler;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderManager;
import org.polyfrost.polydium.client.perf.SpirvCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;
import java.nio.file.Path;

@Mixin(GlslCompiler.class)
public abstract class MixinSpirvCache {
    //? if >= 26.3 {
    @Shadow
    @Final
    private boolean isZeroToOne;

    @Shadow
    @Final
    private boolean shaderDrawParameters;

    @WrapMethod(method = "compileToSpv")
    private SpvModule polydium$cacheSpirv(String name, String source, ShaderType type, ShaderDefines shaderDefines, ShaderSource shaderSource, Operation<SpvModule> original) {
        if (!(shaderSource instanceof ShaderManager.Configs configs)) {
            return original.call(name, source, type, shaderDefines, shaderSource);
        }
        Path file = SpirvCache.file(this.isZeroToOne, this.shaderDrawParameters, name, source, type, shaderDefines, configs);
        if (file == null) {
            return original.call(name, source, type, shaderDefines, shaderSource);
        }
        ByteBuffer cached = SpirvCache.read(file);
        if (cached != null) {
            return new SPIRVModule(cached, type);
        }
        SpvModule module = original.call(name, source, type, shaderDefines, shaderSource);
        SpirvCache.write(file, module.spv());
        return module;
    }
    //?} else {
    /*@WrapMethod(method = "createIntermediary")
    private IntermediaryShaderModule polydium$cacheSpirv(String name, String source, ShaderType type, Operation<IntermediaryShaderModule> original) throws ShaderCompileException {
        Path file = SpirvCache.file(name, source, type);
        if (file == null) {
            return original.call(name, source, type);
        }
        ByteBuffer cached = SpirvCache.read(file);
        if (cached != null) {
            try {
                return IntermediaryShaderModule.createFromSpirv(name, cached);
            } catch (ShaderCompileException e) {
                MemoryUtil.memFree(cached);
            }
        }
        IntermediaryShaderModule module = original.call(name, source, type);
        SpirvCache.write(file, module.spirv());
        return module;
    }
    *///?}
}
//?}
