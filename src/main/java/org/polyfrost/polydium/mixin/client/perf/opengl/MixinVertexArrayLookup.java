package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(targets = {"com.mojang.blaze3d.opengl.VertexArrayCache$Separate", "com.mojang.blaze3d.opengl.VertexArrayCache$Emulated"})
public abstract class MixinVertexArrayLookup {
    @Unique
    private Object[] polydium$lastBindings;
    @Unique
    private Object polydium$lastArray;

    @WrapOperation(method = "bindVertexArray", at = @At(value = "INVOKE", target = "Ljava/util/Arrays;asList([Ljava/lang/Object;)Ljava/util/List;"))
    private List<?> polydium$skipKey(Object[] bindings, Operation<List<?>> original) {
        return bindings == polydium$lastBindings ? null : original.call((Object) bindings);
    }

    @WrapOperation(method = "bindVertexArray", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object polydium$reuseLast(Map<?, ?> cache, Object key, Operation<Object> original, @Local(argsOnly = true) VertexFormat[] bindings) {
        if (key == null) {
            return polydium$lastArray;
        }

        Object array = original.call(cache, key);
        if (array != null) {
            polydium$lastBindings = bindings;
            polydium$lastArray = array;
        }

        return array;
    }

    @WrapOperation(method = "bindVertexArray", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindVertexArray(I)V", ordinal = 1))
    private void polydium$bindIfChanged(int id, Operation<Void> original, @Local(argsOnly = true) VertexArrayCache.VertexArray lastBound) {
        if (lastBound != polydium$lastArray) {
            original.call(id);
        }
    }
}
*///?}
