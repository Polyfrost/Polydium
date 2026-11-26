package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.util.TransientBlockAllocator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlTransientMemory$PersistentMapping")
public abstract class MixinGlTransientRotateOrder {
    @Shadow
    @Final
    private TransientBlockAllocator<?> stagingBlockAllocator;

    @Shadow
    @Final
    private TransientBlockAllocator<?> gpuBlockAllocator;

    @Shadow
    @Final
    private TransientBlockAllocator<?> gpuMappedBlockAllocator;

    @Unique
    private Runnable polydium$stagingRotation;

    @Unique
    private Runnable polydium$gpuRotation;

    @Unique
    private Runnable polydium$gpuMappedRotation;

    @Inject(method = "rotate", at = @At("HEAD"))
    private void polydium$rotateBeforeRecycle(CallbackInfo ci) {
        this.polydium$stagingRotation = this.stagingBlockAllocator.rotate();
        this.polydium$gpuRotation = this.gpuBlockAllocator.rotate();
        this.polydium$gpuMappedRotation = this.gpuMappedBlockAllocator.rotate();
    }

    @WrapOperation(method = "rotate", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/util/TransientBlockAllocator;rotate()Ljava/lang/Runnable;"))
    private Runnable polydium$useEarlyRotation(TransientBlockAllocator<?> allocator, Operation<Runnable> original) {
        Runnable rotation;

        if (allocator == this.stagingBlockAllocator) {
            rotation = this.polydium$stagingRotation;
            this.polydium$stagingRotation = null;
        } else if (allocator == this.gpuBlockAllocator) {
            rotation = this.polydium$gpuRotation;
            this.polydium$gpuRotation = null;
        } else if (allocator == this.gpuMappedBlockAllocator) {
            rotation = this.polydium$gpuMappedRotation;
            this.polydium$gpuMappedRotation = null;
        } else {
            rotation = null;
        }

        return rotation != null ? rotation : original.call(allocator);
    }
}
//?}
