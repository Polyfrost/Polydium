package org.polyfrost.polydium.client.perf;

//? if < 26.3 {
/*import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class EntityCullLerp {
    public static float partialTick = Float.NaN;

    private EntityCullLerp() {
    }

    public static void begin(float partialTicks) {
        partialTick = partialTicks;
    }

    public static void end() {
        partialTick = Float.NaN;
    }

    public static AABB lerp(Entity entity, AABB box) {
        float partialTicks = partialTick;
        if (Float.isNaN(partialTicks) || entity.xOld == entity.getX() && entity.yOld == entity.getY() && entity.zOld == entity.getZ()) {
            return box;
        }
        return ((CullBoxMemo) entity).polydium$moveCullBox(box,
                Mth.lerp((double) partialTicks, entity.xOld, entity.getX()) - entity.getX(),
                Mth.lerp((double) partialTicks, entity.yOld, entity.getY()) - entity.getY(),
                Mth.lerp((double) partialTicks, entity.zOld, entity.getZ()) - entity.getZ()
        );
    }
}
*///?}
