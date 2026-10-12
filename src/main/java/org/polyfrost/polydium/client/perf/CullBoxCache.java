package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.world.phys.AABB;

public interface CullBoxCache {
    AABB polydium$inflateCullBox(AABB base, double amount);
}
//?}
