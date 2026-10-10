package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.gizmos.GizmoCollector;

public final class GizmoCollectorCache {
    private static GizmoCollector renderCollector;

    private GizmoCollectorCache() {
    }

    public static Object get(ThreadLocal<?> collector) {
        return RenderSystem.isOnRenderThread() ? renderCollector : collector.get();
    }

    public static void set(Object value) {
        if (RenderSystem.isOnRenderThread()) {
            renderCollector = (GizmoCollector) value;
        }
    }
}
//?}
