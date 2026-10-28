package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.MappableRingBuffer;
//? if >= 26.1 {
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//?} else {
/*import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
*///?}
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.polyfrost.polydium.client.perf.LevelProjectionRing;
import org.polyfrost.polydium.client.perf.UniformRing;
import org.spongepowered.asm.mixin.Mixin;
//? if >= 26.1
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >= 26.1 {
@Mixin(ProjectionMatrixBuffer.class)
//?} else {
/*@Mixin(PerspectiveProjectionMatrixBuffer.class)
*///?}
public abstract class MixinLevelProjectionRing implements LevelProjectionRing {
    //? if >= 26.1 {
    @Shadow
    private Projection lastUploadedProjection;

    @Shadow
    private long projectionMatrixVersion;
    //?}

    @Unique
    private boolean polydium$level;

    @Unique
    private MappableRingBuffer polydium$ring;

    @Override
    public void polydium$useRing() {
        this.polydium$level = true;
    }

    @Inject(method = "getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;", at = @At("HEAD"), cancellable = true)
    private void polydium$writeRing(Matrix4f projectionMatrix, CallbackInfoReturnable<GpuBufferSlice> cir) {
        if (!this.polydium$level) {
            return;
        }
        //? if >= 26.1 {
        this.lastUploadedProjection = null;
        this.projectionMatrixVersion = -1L;
        //?}
        if (this.polydium$ring == null) {
            this.polydium$ring = new MappableRingBuffer(() -> "Level Projection UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            cir.setReturnValue(UniformRing.write(this.polydium$ring, Std140Builder.onStack(stack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f(projectionMatrix).get()).slice());
        }
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void polydium$closeRing(CallbackInfo ci) {
        if (this.polydium$ring != null) {
            this.polydium$ring.close();
            this.polydium$ring = null;
        }
    }
}
//?}
