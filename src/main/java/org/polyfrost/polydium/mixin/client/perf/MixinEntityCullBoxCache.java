package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.client.perf.CullBoxCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class MixinEntityCullBoxCache implements CullBoxCache {
    @Unique
    private AABB polydium$cullBase;
    @Unique
    private double polydium$cullAmount;
    @Unique
    private AABB polydium$cullBox;

    @Override
    public AABB polydium$inflateCullBox(AABB base, double amount) {
        if (base != this.polydium$cullBase || amount != this.polydium$cullAmount) {
            this.polydium$cullBox = base.inflate(amount);
            this.polydium$cullBase = base;
            this.polydium$cullAmount = amount;
        }
        return this.polydium$cullBox;
    }
}
//?}
