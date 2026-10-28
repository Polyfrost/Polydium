package org.polyfrost.polydium.client.perf;

import net.minecraft.world.phys.AABB;

public interface CullBoxMemo {
    AABB polydium$moveCullBox(AABB base, double dx, double dy, double dz);
}
