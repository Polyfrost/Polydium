package org.polyfrost.polydium.client.perf;

//? if < 26.3 {
/*import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.polyfrost.polydium.mixin.client.perf.AABBAccessor;

public final class EntityCullLerp {
    public static float partialTick = Float.NaN;

    private static final boolean IN_PLACE = !PolydiumMixinPlugin.isDisabled("org.polyfrost.polydium.mixin.client.perf.AABBAccessor");

    private static AABB moved;

    private EntityCullLerp() {
    }

    public static void begin(float partialTicks) {
        partialTick = partialTicks;
    }

    public static void end() {
        partialTick = Float.NaN;
        moved = null;
    }

    public static AABB lerp(Entity entity, AABB box) {
        float partialTicks = partialTick;
        if (Float.isNaN(partialTicks) || entity.xOld == entity.getX() && entity.yOld == entity.getY() && entity.zOld == entity.getZ()) {
            return box;
        }
        AABB lerped = box.move(
                Mth.lerp((double) partialTicks, entity.xOld, entity.getX()) - entity.getX(),
                Mth.lerp((double) partialTicks, entity.yOld, entity.getY()) - entity.getY(),
                Mth.lerp((double) partialTicks, entity.zOld, entity.getZ()) - entity.getZ()
        );
        if (IN_PLACE) {
            moved = lerped;
        }
        return lerped;
    }

    public static AABB inflateMoved(AABB box, double amount) {
        if (box != moved) {
            return null;
        }
        moved = null;
        AABBAccessor accessor = (AABBAccessor) box;
        accessor.polydium$setMinX(box.minX - amount);
        accessor.polydium$setMinY(box.minY - amount);
        accessor.polydium$setMinZ(box.minZ - amount);
        accessor.polydium$setMaxX(box.maxX + amount);
        accessor.polydium$setMaxY(box.maxY + amount);
        accessor.polydium$setMaxZ(box.maxZ + amount);
        return box;
    }
}
*///?}
