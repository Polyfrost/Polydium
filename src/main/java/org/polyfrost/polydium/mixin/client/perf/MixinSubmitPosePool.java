package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import org.polyfrost.polydium.client.perf.SubmitPosePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SubmitNodeCollection.class)
public class MixinSubmitPosePool {
    @Redirect(method = "submitModel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;copy()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"))
    private PoseStack.Pose polydium$pooledPose(PoseStack.Pose pose) {
        return SubmitPosePool.copy(pose);
    }
}
//?}
