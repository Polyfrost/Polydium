package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 && < 26.3 {
/*import com.mojang.renderpearl.backend.util.TransientBlockAllocator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.polyfrost.polydium.client.perf.TransientHeapPlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransientBlockAllocator.class)
public abstract class MixinTransientBlockRecycle<T> {

    @Shadow
    @Final
    private TransientBlockAllocator.Allocator<T> allocator;

    @Shadow
    @Final
    private ReferenceArrayList<T> freeBlocks;

    @Inject(method = "lambda$rotate$1", at = @At("HEAD"), cancellable = true)
    private void polydium$recycleUsedBlocks(ReferenceArrayList<T> used, ReferenceArrayList<T> special, CallbackInfo ci) {
        for (T block : used) {
            if (TransientHeapPlacement.SUBOPTIMAL.remove(block)) {
                this.allocator.free(block);
            } else {
                this.freeBlocks.add(block);
            }
        }

        for (T block : special) {
            TransientHeapPlacement.SUBOPTIMAL.remove(block);
            this.allocator.free(block);
        }

        ci.cancel();
    }
}
*///?}
