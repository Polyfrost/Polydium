package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelPart.class)
public abstract class MixinModelPartIdentity {
    @Shadow
    public float x;
    @Shadow
    public float y;
    @Shadow
    public float z;
    @Shadow
    public float xRot;
    @Shadow
    public float yRot;
    @Shadow
    public float zRot;
    @Shadow
    public float xScale;
    @Shadow
    public float yScale;
    @Shadow
    public float zScale;

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V")
    )
    private void polydium$skipIdentityPush(PoseStack poseStack, Operation<Void> original, @Share("polydium$identity") LocalBooleanRef identity) {
        if (((Object) this).getClass() == ModelPart.class
                && this.x == 0.0F && this.y == 0.0F && this.z == 0.0F
                && this.xRot == 0.0F && this.yRot == 0.0F && this.zRot == 0.0F
                && this.xScale == 1.0F && this.yScale == 1.0F && this.zScale == 1.0F) {
            identity.set(true);
            return;
        }
        original.call(poseStack);
    }

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void polydium$skipIdentityPop(PoseStack poseStack, Operation<Void> original, @Share("polydium$identity") LocalBooleanRef identity) {
        if (!identity.get()) {
            original.call(poseStack);
        }
    }
}
//?}
