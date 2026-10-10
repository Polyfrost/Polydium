package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;

@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public class MixinRenderTypeEquality {
    @Unique
    private static final int POLYDIUM_LINEAR_SEARCH_LIMIT = 8;

    @Unique
    private Object2IntOpenHashMap<PreparedRenderType> polydium$index;

    @WrapOperation(method = "getOrAddDraw", at = @At(value = "INVOKE", target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I"))
    private int polydium$indexOfPrepared(List<?> list, Object value, Operation<Integer> original) {
        Object2IntOpenHashMap<PreparedRenderType> index = this.polydium$index;
        if (index != null && value instanceof PreparedRenderType) {
            return index.getInt(value);
        }
        if (!(value instanceof PreparedRenderType wanted)) {
            return original.call(list, value);
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            Object entry = list.get(i);
            if (entry == wanted || entry instanceof PreparedRenderType other && polydium$same(wanted, other)) {
                return i;
            }
        }
        return -1;
    }

    @WrapOperation(method = "getOrAddDraw", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean polydium$indexAdded(List<Object> list, Object value, Operation<Boolean> original) {
        boolean added = original.call(list, value);
        Object2IntOpenHashMap<PreparedRenderType> index = this.polydium$index;
        if (index != null) {
            if (value instanceof PreparedRenderType type) {
                index.putIfAbsent(type, list.size() - 1);
            }
            return added;
        }
        if (list.size() > POLYDIUM_LINEAR_SEARCH_LIMIT) {
            index = new Object2IntOpenHashMap<>(list.size() * 2);
            index.defaultReturnValue(-1);
            for (int position = 0, size = list.size(); position < size; position++) {
                if (list.get(position) instanceof PreparedRenderType type) {
                    index.putIfAbsent(type, position);
                }
            }
            this.polydium$index = index;
        }
        return added;
    }

    @Unique
    private static boolean polydium$same(PreparedRenderType a, PreparedRenderType b) {
        return a.pipeline() == b.pipeline()
            && a.scissorState() == b.scissorState()
            && Objects.equals(a.dynamicTransforms(), b.dynamicTransforms())
            //? if >= 26.3 {
            && Objects.equals(a.oitPipelineSet(), b.oitPipelineSet())
            && Objects.equals(a.name(), b.name())
            //?} else {
            /*&& Objects.equals(a.outputTarget(), b.outputTarget())
            *///?}
            && Objects.equals(a.textures(), b.textures());
    }
}
//?}
