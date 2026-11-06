package org.polyfrost.polydium.client.perf;

import net.minecraft.world.phys.AABB;

public interface BlockEntityCullBox {
    AABB polydium$cullBox();

    void polydium$setCullBox(AABB box);
}
