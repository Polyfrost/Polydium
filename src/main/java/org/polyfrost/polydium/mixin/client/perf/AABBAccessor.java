package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AABB.class)
public interface AABBAccessor {
    @Mutable
    @Accessor("minX")
    void polydium$setMinX(double minX);

    @Mutable
    @Accessor("minY")
    void polydium$setMinY(double minY);

    @Mutable
    @Accessor("minZ")
    void polydium$setMinZ(double minZ);

    @Mutable
    @Accessor("maxX")
    void polydium$setMaxX(double maxX);

    @Mutable
    @Accessor("maxY")
    void polydium$setMaxY(double maxY);

    @Mutable
    @Accessor("maxZ")
    void polydium$setMaxZ(double maxZ);
}
//?}
