package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Frustum.class)
public abstract class MixinFrustumBoxTest {
    @Shadow
    @Final
    private FrustumIntersection intersection;
    @Shadow
    private double camX;
    @Shadow
    private double camY;
    @Shadow
    private double camZ;

    @WrapOperation(
            method = "isVisible",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;cubeInFrustum(DDDDDD)I")
    )
    private int polydium$plainBoxTest(Frustum self, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Operation<Integer> original) {
        return this.intersection.testAab(
                (float) (minX - this.camX), (float) (minY - this.camY), (float) (minZ - this.camZ),
                (float) (maxX - this.camX), (float) (maxY - this.camY), (float) (maxZ - this.camZ)
        ) ? FrustumIntersection.INTERSECT : FrustumIntersection.OUTSIDE;
    }
}
//?}
