package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import org.polyfrost.polydium.client.perf.BufferBuilderElements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilderElements {
    @Shadow
    @Final
    private static String[] elementNames;

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/vertex/VertexFormat;getElement(Ljava/lang/String;)Lcom/mojang/renderpearl/api/vertex/VertexFormatElement;")
    )
    private VertexFormatElement polydium$cachedElement(VertexFormat format, String name, Operation<VertexFormatElement> original, @Local(ordinal = 1) int index) {
        String[] names = elementNames;
        if (index < 0 || index >= names.length || names[index] != name) {
            return original.call(format, name);
        }

        BufferBuilderElements holder = (BufferBuilderElements) (Object) format;
        VertexFormatElement[] elements = holder.polydium$getBufferElements();
        if (elements == null) {
            elements = new VertexFormatElement[names.length];
            for (int i = 0; i < names.length; i++) {
                elements[i] = original.call(format, names[i]);
            }
            holder.polydium$setBufferElements(elements);
        }
        return elements[index];
    }
}
//?}
