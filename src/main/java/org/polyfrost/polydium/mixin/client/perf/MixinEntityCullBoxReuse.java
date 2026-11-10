package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityCullBoxReuse {
    @Unique
    private static final boolean polydium$MUTABLE = !PolydiumMixinPlugin.isDisabled("org.polyfrost.polydium.mixin.client.perf.AABBAccessor");
    @Unique
    private static final AABB polydium$cullBox = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

    @WrapOperation(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private AABB polydium$reuseCullBox(AABB base, double amount, Operation<AABB> original) {
        if (!polydium$MUTABLE || !RenderSystem.isOnRenderThread()) {
            return original.call(base, amount);
        }
        double minX = base.minX - amount;
        double minY = base.minY - amount;
        double minZ = base.minZ - amount;
        double maxX = base.maxX + amount;
        double maxY = base.maxY + amount;
        double maxZ = base.maxZ + amount;
        AABBAccessor box = (AABBAccessor) polydium$cullBox;
        box.polydium$setMinX(Math.min(minX, maxX));
        box.polydium$setMinY(Math.min(minY, maxY));
        box.polydium$setMinZ(Math.min(minZ, maxZ));
        box.polydium$setMaxX(Math.max(minX, maxX));
        box.polydium$setMaxY(Math.max(minY, maxY));
        box.polydium$setMaxZ(Math.max(minZ, maxZ));
        return polydium$cullBox;
    }
}
//?}
