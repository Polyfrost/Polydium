package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import org.polyfrost.polydium.client.perf.ZyxRotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.api.math.MatrixHelper", remap = false)
public abstract class MixinMatrixHelperSharedTrig {
    @WrapMethod(method = "rotateZYX(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;FFF)V", remap = false)
    private static void polydium$rotateSharedTrig(PoseStack.Pose matrices, float angleZ, float angleY, float angleX, Operation<Void> original) {
        ZyxRotation.rotate(matrices.pose(), matrices.normal(), angleZ, angleY, angleX);
    }
}
