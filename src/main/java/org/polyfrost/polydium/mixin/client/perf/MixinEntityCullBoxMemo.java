package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
//?}
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.CullBoxMemo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class MixinEntityCullBoxMemo implements CullBoxMemo {
    @Unique
    private AABB polydium$memoBase;
    @Unique
    private long polydium$memoDx;
    @Unique
    private long polydium$memoDy;
    @Unique
    private long polydium$memoDz;
    @Unique
    private AABB polydium$memoBox;

    @Override
    public AABB polydium$moveCullBox(AABB base, double dx, double dy, double dz) {
        if (!RenderSystem.isOnRenderThread()) {
            return base.move(dx, dy, dz);
        }
        long bx = Double.doubleToRawLongBits(dx);
        long by = Double.doubleToRawLongBits(dy);
        long bz = Double.doubleToRawLongBits(dz);
        if (base != this.polydium$memoBase || bx != this.polydium$memoDx || by != this.polydium$memoDy || bz != this.polydium$memoDz) {
            this.polydium$memoBox = base.move(dx, dy, dz);
            this.polydium$memoBase = base;
            this.polydium$memoDx = bx;
            this.polydium$memoDy = by;
            this.polydium$memoDz = bz;
        }
        return this.polydium$memoBox;
    }

    //? if >= 26.3 {
    @Redirect(method = "getInterpolatedBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB polydium$memoInterpolatedBox(AABB base, double dx, double dy, double dz) {
        return this.polydium$moveCullBox(base, dx, dy, dz);
    }
    //?}
}
