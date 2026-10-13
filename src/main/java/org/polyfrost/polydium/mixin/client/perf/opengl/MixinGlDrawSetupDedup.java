package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.llamalad7.mixinextras.sugar.Local;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.polyfrost.polydium.compat.Mods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class MixinGlDrawSetupDedup {
    @Unique
    private static final boolean polydium$IRIS = Mods.loaded("iris");

    @Shadow
    private GlProgram lastProgram;
    @Shadow
    private VertexArrayCache.VertexArray lastVertexArray;

    @Unique
    private GlProgram polydium$programBefore;
    @Unique
    private GlProgram polydium$texturesProgram;
    @Unique
    private Map<?, ?> polydium$textures;
    @Unique
    private Set<?> polydium$passDirty;
    @Unique
    private GlRenderPass polydium$dirtyPass;
    @Unique
    private boolean polydium$drawBuffersDone;
    @Unique
    private boolean polydium$scissorSet;
    @Unique
    private int polydium$scissorX;
    @Unique
    private int polydium$scissorY;
    @Unique
    private int polydium$scissorWidth;
    @Unique
    private int polydium$scissorHeight;
    @Unique
    private Object polydium$indexArray;
    @Unique
    private int polydium$indexHandle;

    @Inject(method = "createRenderPass", at = @At("HEAD"))
    private void polydium$resetPassState(CallbackInfoReturnable<?> cir) {
        polydium$passDirty = null;
        polydium$drawBuffersDone = false;
        polydium$scissorSet = false;
    }

    @Inject(method = "trySetup", at = @At("HEAD"))
    private void polydium$captureProgram(CallbackInfoReturnable<Boolean> cir) {
        polydium$programBefore = lastProgram;
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlProgram;getUniforms()Ljava/util/Map;", ordinal = 1))
    private Map<?, ?> polydium$skipCleanUniforms(GlProgram program, Operation<Map<?, ?>> original, @Local(argsOnly = true) GlRenderPass pass) {
        Set<?> dirty = polydium$passDirty;
        if (dirty != null && pass == polydium$dirtyPass && dirty.isEmpty() && program == polydium$programBefore && !IrisShaders.packInUse()) {
            return polydium$texturesOf(program, original);
        }

        return original.call(program);
    }

    @Unique
    private Map<?, ?> polydium$texturesOf(GlProgram program, Operation<Map<?, ?>> original) {
        if (program != polydium$texturesProgram) {
            Map<Object, Object> textures = new HashMap<>();
            for (Map.Entry<?, ?> entry : original.call(program).entrySet()) {
                if (!(entry.getValue() instanceof Uniform.Ubo)) {
                    textures.put(entry.getKey(), entry.getValue());
                }
            }

            polydium$textures = textures;
            polydium$texturesProgram = program;
        }

        return polydium$textures;
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    private void polydium$trackDirtySet(Set<?> dirty, Operation<Void> original, @Local(argsOnly = true) GlRenderPass pass) {
        polydium$passDirty = dirty;
        polydium$dirtyPass = pass;
        original.call(dirty);
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawBuffers([I)V"))
    private void polydium$drawBuffersOnce(int[] buffers, Operation<Void> original) {
        if (polydium$IRIS || !polydium$drawBuffersDone) {
            polydium$drawBuffersDone = true;
            original.call((Object) buffers);
        }
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_scissorBox(IIII)V"))
    private void polydium$scissorIfChanged(int x, int y, int width, int height, Operation<Void> original) {
        if (polydium$scissorSet && x == polydium$scissorX && y == polydium$scissorY && width == polydium$scissorWidth && height == polydium$scissorHeight && !IrisShaders.packInUse()) {
            return;
        }

        polydium$scissorSet = true;
        polydium$scissorX = x;
        polydium$scissorY = y;
        polydium$scissorWidth = width;
        polydium$scissorHeight = height;
        original.call(x, y, width, height);
    }

    @Inject(method = {"executeDraw", "executeDrawMultiple"}, at = @At("HEAD"))
    private void polydium$resetIndexBinding(CallbackInfo ci) {
        polydium$indexHandle = -1;
    }

    @WrapOperation(method = "drawFromBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindBuffer(II)V"))
    private void polydium$bindIndexIfChanged(int target, int handle, Operation<Void> original) {
        if (handle == polydium$indexHandle && lastVertexArray == polydium$indexArray) {
            return;
        }

        polydium$indexArray = lastVertexArray;
        polydium$indexHandle = handle;
        original.call(target, handle);
    }
}
*///?} else if >= 1.21.8 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.llamalad7.mixinextras.sugar.Local;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class MixinGlDrawSetupDedup {
    @Shadow
    private GlProgram lastProgram;

    @Unique
    private GlProgram polydium$programBefore;
    @Unique
    private GlProgram polydium$texturesProgram;
    @Unique
    private Map<?, ?> polydium$textures;
    @Unique
    private Set<?> polydium$passDirty;
    @Unique
    private GlRenderPass polydium$dirtyPass;
    @Unique
    private boolean polydium$scissorSet;
    @Unique
    private int polydium$scissorX;
    @Unique
    private int polydium$scissorY;
    @Unique
    private int polydium$scissorWidth;
    @Unique
    private int polydium$scissorHeight;
    @Unique
    private VertexFormat polydium$vertexFormat;
    @Unique
    private GlBuffer polydium$vertexBuffer;
    @Unique
    private int polydium$indexHandle;

    @Inject(method = "createRenderPass", at = @At("HEAD"))
    private void polydium$resetPassState(CallbackInfoReturnable<?> cir) {
        polydium$passDirty = null;
        polydium$scissorSet = false;
    }

    @Inject(method = "trySetup", at = @At("HEAD"))
    private void polydium$captureProgram(CallbackInfoReturnable<Boolean> cir) {
        polydium$programBefore = lastProgram;
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlProgram;getUniforms()Ljava/util/Map;", ordinal = 1))
    private Map<?, ?> polydium$skipCleanUniforms(GlProgram program, Operation<Map<?, ?>> original, @Local(argsOnly = true) GlRenderPass pass) {
        Set<?> dirty = polydium$passDirty;
        if (dirty != null && pass == polydium$dirtyPass && dirty.isEmpty() && program == polydium$programBefore && !IrisShaders.packInUse()) {
            return polydium$texturesOf(program, original);
        }

        return original.call(program);
    }

    @Unique
    private Map<?, ?> polydium$texturesOf(GlProgram program, Operation<Map<?, ?>> original) {
        if (program != polydium$texturesProgram) {
            Map<Object, Object> textures = new HashMap<>();
            for (Map.Entry<?, ?> entry : original.call(program).entrySet()) {
                if (!(entry.getValue() instanceof Uniform.Ubo)) {
                    textures.put(entry.getKey(), entry.getValue());
                }
            }

            polydium$textures = textures;
            polydium$texturesProgram = program;
        }

        return polydium$textures;
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    private void polydium$trackDirtySet(Set<?> dirty, Operation<Void> original, @Local(argsOnly = true) GlRenderPass pass) {
        polydium$passDirty = dirty;
        polydium$dirtyPass = pass;
        original.call(dirty);
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_scissorBox(IIII)V"))
    private void polydium$scissorIfChanged(int x, int y, int width, int height, Operation<Void> original) {
        if (polydium$scissorSet && x == polydium$scissorX && y == polydium$scissorY && width == polydium$scissorWidth && height == polydium$scissorHeight && !IrisShaders.packInUse()) {
            return;
        }

        polydium$scissorSet = true;
        polydium$scissorX = x;
        polydium$scissorY = y;
        polydium$scissorWidth = width;
        polydium$scissorHeight = height;
        original.call(x, y, width, height);
    }

    @Inject(method = {"executeDraw", "executeDrawMultiple"}, at = @At("HEAD"))
    private void polydium$resetDrawBindings(CallbackInfo ci) {
        polydium$vertexFormat = null;
        polydium$indexHandle = -1;
    }

    @WrapOperation(method = "drawFromBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/VertexArrayCache;bindVertexArray(Lcom/mojang/renderpearl/api/vertex/VertexFormat;Lcom/mojang/renderpearl/backend/opengl/GlBuffer;)V"))
    private void polydium$bindVertexArrayIfChanged(VertexArrayCache cache, VertexFormat format, GlBuffer buffer, Operation<Void> original) {
        if (format == polydium$vertexFormat && buffer == polydium$vertexBuffer) {
            return;
        }

        polydium$vertexFormat = format;
        polydium$vertexBuffer = buffer;
        original.call(cache, format, buffer);
    }

    @WrapOperation(method = "drawFromBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindBuffer(II)V"))
    private void polydium$bindIndexIfChanged(int target, int handle, Operation<Void> original) {
        if (handle == polydium$indexHandle) {
            return;
        }

        polydium$indexHandle = handle;
        original.call(target, handle);
    }
}
*///?} else if >= 1.21.5 && < 1.21.8 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class MixinGlDrawSetupDedup {
    @Unique
    private boolean polydium$scissorSet;
    @Unique
    private int polydium$scissorX;
    @Unique
    private int polydium$scissorY;
    @Unique
    private int polydium$scissorWidth;
    @Unique
    private int polydium$scissorHeight;
    @Unique
    private VertexFormat polydium$vertexFormat;
    @Unique
    private GlBuffer polydium$vertexBuffer;
    @Unique
    private int polydium$indexHandle;

    @Inject(method = "createRenderPass", at = @At("HEAD"))
    private void polydium$resetPassState(CallbackInfoReturnable<?> cir) {
        polydium$scissorSet = false;
    }

    @WrapOperation(method = "trySetup", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_scissorBox(IIII)V"))
    private void polydium$scissorIfChanged(int x, int y, int width, int height, Operation<Void> original) {
        if (polydium$scissorSet && x == polydium$scissorX && y == polydium$scissorY && width == polydium$scissorWidth && height == polydium$scissorHeight && !IrisShaders.packInUse()) {
            return;
        }

        polydium$scissorSet = true;
        polydium$scissorX = x;
        polydium$scissorY = y;
        polydium$scissorWidth = width;
        polydium$scissorHeight = height;
        original.call(x, y, width, height);
    }

    @Inject(method = {"executeDraw", "executeDrawMultiple"}, at = @At("HEAD"))
    private void polydium$resetDrawBindings(CallbackInfo ci) {
        polydium$vertexFormat = null;
        polydium$indexHandle = -1;
    }

    @WrapOperation(method = "drawFromBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/VertexArrayCache;bindVertexArray(Lcom/mojang/renderpearl/api/vertex/VertexFormat;Lcom/mojang/renderpearl/backend/opengl/GlBuffer;)V"))
    private void polydium$bindVertexArrayIfChanged(VertexArrayCache cache, VertexFormat format, GlBuffer buffer, Operation<Void> original) {
        if (format == polydium$vertexFormat && buffer == polydium$vertexBuffer) {
            return;
        }

        polydium$vertexFormat = format;
        polydium$vertexBuffer = buffer;
        original.call(cache, format, buffer);
    }

    @WrapOperation(method = "drawFromBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindBuffer(II)V"))
    private void polydium$bindIndexIfChanged(int target, int handle, Operation<Void> original) {
        if (handle == polydium$indexHandle) {
            return;
        }

        polydium$indexHandle = handle;
        original.call(target, handle);
    }
}
*///?}
