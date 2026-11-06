package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Mixin(ModelFeatureRenderer.class)
public class MixinTranslucentModelSortKeys {
    @Unique
    private long[] polydium$sortKeys = new long[0];

    @WrapOperation(method = {"render", "renderTranslucent"}, at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"))
    private void polydium$sortByCachedDistance(List<SubmitNodeStorage.TranslucentModelSubmit<?>> submits, Comparator<?> comparator, Operation<Void> original) {
        int size = submits.size();
        if (size < 2) {
            return;
        }
        long[] keys = this.polydium$sortKeys;
        if (keys.length < size) {
            keys = this.polydium$sortKeys = new long[Math.max(size, keys.length * 2)];
        }
        for (int i = 0; i < size; i++) {
            int bits = Float.floatToIntBits(-submits.get(i).position().lengthSquared());
            bits ^= (bits >> 31) & 0x7fffffff;
            keys[i] = ((long) bits << 32) | i;
        }
        Arrays.sort(keys, 0, size);
        Object[] snapshot = submits.toArray();
        for (int i = 0; i < size; i++) {
            submits.set(i, (SubmitNodeStorage.TranslucentModelSubmit<?>) snapshot[(int) keys[i]]);
        }
    }
}
*///?}
