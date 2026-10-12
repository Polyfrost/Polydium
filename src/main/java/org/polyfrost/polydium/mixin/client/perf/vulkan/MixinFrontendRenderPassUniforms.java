package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.8 {
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.polyfrost.polydium.client.perf.PipelineUniforms;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
//?} else if >= 26.2 {
/*import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Set;
*///?} else {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Set;
*///?}

//? if >= 26.3 {
@Mixin(FrontendRenderPass.class)
public abstract class MixinFrontendRenderPassUniforms {
    @Unique
    private static long polydium$passes;

    @Shadow
    @Final
    private RenderPassBackend backend;

    @Shadow
    @Final
    private List<RenderPassDescriptor.Attachment<Optional<Vector4fc>>> colorAttachments;

    @Shadow
    private FrontendRenderPipeline boundPipeline;

    @Shadow
    @Final
    protected HashMap<String, Object> uniforms;

    @Shadow
    private boolean constantsPushed;

    @Unique
    private long polydium$pass;

    @Unique
    private boolean polydium$reapplying;

    @Unique
    private boolean polydium$sameValue;

    @Unique
    private byte polydium$backendKind;

    @Unique
    private boolean polydium$tracksBackend() {
        byte kind = this.polydium$backendKind;
        if (kind == 0) {
            boolean tracked = this.backend instanceof VulkanRenderPass
                || "com.mojang.renderpearl.backend.opengl.GlRenderPass".equals(this.backend.getClass().getName()) && !IrisShaders.packInUse();
            kind = this.polydium$backendKind = (byte) (tracked ? 1 : 2);
        }
        return kind == 1;
    }

    @Shadow
    private void setUniform(String name, @Nullable Object value) {
        throw new AssertionError();
    }

    @Overwrite
    public void setPipeline(final CompiledRenderPipeline pipeline) {
        boolean tracked = this.polydium$tracksBackend();
        if (tracked && pipeline != null && pipeline == this.boundPipeline) {
            this.constantsPushed = false;
            return;
        }
        if (!(pipeline instanceof FrontendRenderPipeline frontendPipeline)) {
            throw new IllegalArgumentException("Pipeline must be instance of FrontendCompiledRenderPipeline");
        }
        boolean trim = tracked;
        PipelineUniforms layout = (PipelineUniforms) (Object) frontendPipeline;
        if (trim && this.polydium$pass == 0L) {
            this.polydium$pass = ++polydium$passes;
        }
        if (!trim || layout.polydium$validatedPass() != this.polydium$pass) {
            List<ColorTargetState> colorTargetStates = frontendPipeline.colorTargetStates();
            if (colorTargetStates.size() != this.colorAttachments.size()) {
                throw new IllegalStateException("Render pass color attachment count must match pipeline color target state count.");
            }
            for (int i = 0; i < this.colorAttachments.size(); i++) {
                RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment = this.colorAttachments.get(i);
                if (attachment != null) {
                    ColorTargetState colorTargetState = colorTargetStates.get(i);
                    if (colorTargetState == null || colorTargetState.format() != attachment.textureView().texture().getFormat()) {
                        throw new IllegalStateException("Render pass color attachment " + i + " format doesn't match pipeline format.");
                    }
                }
            }
            if (trim) {
                layout.polydium$validatedPass(this.polydium$pass);
            }
        }
        this.boundPipeline = frontendPipeline;
        this.backend.setPipeline(frontendPipeline.backendRenderPipeline());
        if (trim) {
            String[] names = layout.polydium$uniformNames();
            int[] slots = layout.polydium$uniformSlots();
            for (int i = 0; i < names.length; i++) {
                Object value = this.uniforms.get(names[i]);
                if (value != null) {
                    this.backend.setUniform(slots[i], value);
                }
            }
        } else {
            this.polydium$reapplying = true;
            try {
                for (Map.Entry<String, Object> uniform : this.uniforms.entrySet()) {
                    this.setUniform(uniform.getKey(), uniform.getValue());
                }
            } finally {
                this.polydium$reapplying = false;
            }
        }
        this.constantsPushed = false;
    }

    @Overwrite
    public void setUniform(final String name, final @Nullable GpuTextureView textureView, final @Nullable GpuSampler sampler) {
        if (textureView != null && sampler != null) {
            if (this.polydium$tracksBackend()
                && this.uniforms.get(name) instanceof TextureViewAndSampler bound && bound.view() == textureView && bound.sampler() == sampler) {
                return;
            }
            this.setUniform(name, new TextureViewAndSampler(textureView, sampler));
        } else {
            if (textureView != null || sampler != null) {
                throw new IllegalArgumentException("textureView and sampler must both or neither be null");
            }
            this.setUniform(name, (Object) null);
        }
    }

    @ModifyExpressionValue(
        method = "setUniform(Ljava/lang/String;Ljava/lang/Object;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/HashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object polydium$compareWithPrevious(Object previous, @Local(argsOnly = true) Object value) {
        this.polydium$sameValue = !this.polydium$reapplying && value.equals(previous)
            && this.polydium$tracksBackend();
        return previous;
    }

    @WrapWithCondition(
        method = "setUniform(Ljava/lang/String;Ljava/lang/Object;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/api/RenderPassBackend;setUniform(ILjava/lang/Object;)V")
    )
    private boolean polydium$storeChangedUniform(RenderPassBackend backend, int index, Object value) {
        boolean same = this.polydium$sameValue;
        this.polydium$sameValue = false;
        return value == null || !same;
    }
}
//?} else if >= 26.2 {
/*@Mixin(targets = {"com.mojang.renderpearl.backend.vulkan.VulkanRenderPass", "com.mojang.renderpearl.backend.opengl.GlRenderPass"})
public abstract class MixinFrontendRenderPassUniforms {
    @Unique
    private boolean polydium$sameValue;

    @Unique
    private RenderPipeline polydium$pipeline;

    @Unique
    private boolean polydium$samePipeline;

    @WrapOperation(
        method = {
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
            "bindTexture"
        },
        at = @At(value = "INVOKE", target = "Ljava/util/HashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object polydium$compareWithPrevious(HashMap<Object, Object> map, Object name, Object value, Operation<Object> original) {
        Object previous = original.call(map, name, value);
        this.polydium$sameValue = value.equals(previous) && ((Object) this instanceof VulkanRenderPass || !IrisShaders.packInUse());
        return previous;
    }

    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void polydium$compareWithBound(RenderPipeline pipeline, CallbackInfo ci) {
        this.polydium$samePipeline = pipeline == this.polydium$pipeline;
        this.polydium$pipeline = pipeline;
    }

    @WrapWithCondition(
        method = "setPipeline",
        at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanRenderPass;anyDescriptorDirty:Z", opcode = Opcodes.PUTFIELD),
        require = 0
    )
    private boolean polydium$markChangedPipeline(VulkanRenderPass pass, boolean dirty) {
        return !this.polydium$samePipeline;
    }

    @WrapWithCondition(
        method = {
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
            "bindTexture"
        },
        at = @At(value = "FIELD", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanRenderPass;anyDescriptorDirty:Z", opcode = Opcodes.PUTFIELD),
        require = 0
    )
    private boolean polydium$markChangedDescriptor(VulkanRenderPass pass, boolean dirty) {
        boolean same = this.polydium$sameValue;
        this.polydium$sameValue = false;
        return !same;
    }

    @WrapOperation(
        method = {
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
            "bindTexture"
        },
        at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"),
        require = 0
    )
    private boolean polydium$markChangedUniform(Set<Object> dirty, Object name, Operation<Boolean> original) {
        boolean same = this.polydium$sameValue;
        this.polydium$sameValue = false;
        return !same && original.call(dirty, name);
    }
}
*///?} else {
/*@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlRenderPass")
public abstract class MixinFrontendRenderPassUniforms {
    @Unique
    private boolean polydium$sameValue;

    @WrapOperation(
        method = {
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
            /^? if >= 1.21.11 {^/"bindTexture"/^?} else {^//^"bindSampler"^//^?}^/
        },
        at = @At(value = "INVOKE", target = "Ljava/util/HashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object polydium$compareWithPrevious(HashMap<Object, Object> map, Object name, Object value, Operation<Object> original) {
        Object previous = original.call(map, name, value);
        this.polydium$sameValue = value.equals(previous) && !org.polyfrost.polydium.client.perf.IrisShaders.packInUse();
        return previous;
    }

    @WrapOperation(
        method = {
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V",
            "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
            /^? if >= 1.21.11 {^/"bindTexture"/^?} else {^//^"bindSampler"^//^?}^/
        },
        at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z")
    )
    private boolean polydium$markChangedUniform(Set<Object> dirty, Object name, Operation<Boolean> original) {
        boolean same = this.polydium$sameValue;
        this.polydium$sameValue = false;
        return !same && original.call(dirty, name);
    }
}
*///?}
//?}
