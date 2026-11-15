package org.polyfrost.polydium.client.perf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.atomic.AtomicInteger;

public interface BlockEntityCullBox {
    AtomicInteger RENDERER_EPOCH = new AtomicInteger(1);

    ClassValue<Boolean> FIXED_BOUNDS = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> renderer) {
            try {
                return renderer.getMethod("getRenderBoundingBox", BlockEntity.class).getDeclaringClass().getName()
                        .equals("dev.tr7zw.entityculling.BlockEntityRenderFabricExtension");
            } catch (NoSuchMethodException e) {
                return true;
            } catch (LinkageError e) {
                return false;
            }
        }
    };

    static boolean fixedBounds(Class<?> renderer) {
        return FIXED_BOUNDS.get(renderer);
    }

    AABB polydium$storedCullBox();

    int polydium$cullBoxEpoch();

    void polydium$setCullBox(AABB box, int rendererEpoch);

    default AABB polydium$cullBox(int rendererEpoch) {
        return rendererEpoch == this.polydium$cullBoxEpoch() ? this.polydium$storedCullBox() : null;
    }
}
