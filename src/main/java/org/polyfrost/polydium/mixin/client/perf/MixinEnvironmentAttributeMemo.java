package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.attribute.EnvironmentAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.world.attribute.EnvironmentAttributeProbe$ValueProbe")
public class MixinEnvironmentAttributeMemo {
    @Shadow
    private Object lastValue;
    @Shadow
    private Object newValue;

    @Unique
    private Object polydium$memoLast;
    @Unique
    private Object polydium$memoNew;
    @Unique
    private float polydium$memoPartialTick;
    @Unique
    private Object polydium$memoResult;

    @WrapMethod(method = "get")
    private Object polydium$memoGet(EnvironmentAttribute<?> attribute, float partialTicks, Operation<Object> original) {
        Object next = this.newValue;
        if (next != null && next == this.polydium$memoNew && this.lastValue == this.polydium$memoLast && partialTicks == this.polydium$memoPartialTick) {
            return this.polydium$memoResult;
        }
        Object result = original.call(attribute, partialTicks);
        this.polydium$memoLast = this.lastValue;
        this.polydium$memoNew = this.newValue;
        this.polydium$memoPartialTick = partialTicks;
        this.polydium$memoResult = result;
        return result;
    }
}
//?}
