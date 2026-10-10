package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.lwjgl.system.MemoryStack;
import org.polyfrost.polydium.client.perf.RenderThreadStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MemoryStack.class, remap = false)
public class MixinRenderThreadStack {
    @WrapMethod(method = "stackGet")
    private static MemoryStack polydium$renderThreadStack(Operation<MemoryStack> original) {
        RenderThreadStack.Owner owner = RenderThreadStack.owner();
        if (owner != null && owner.thread() == Thread.currentThread()) {
            return owner.stack();
        }
        return original.call();
    }
}
//?}
