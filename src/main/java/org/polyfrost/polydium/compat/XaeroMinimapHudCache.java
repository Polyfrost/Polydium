package org.polyfrost.polydium.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.PolydiumToggles;

public final class XaeroMinimapHudCache {
    private static final long FRAME_NANOS = 1_000_000_000L / 60;
    private static final long IDLE_NANOS = 1_000_000_000L / 20;

    private long lastRender;
    private int lastKey;
    private float lastYaw;

    public boolean reuse(int key, boolean zooming) {
        if (!PolydiumToggles.xaeroMinimapHudCache) return false;
        Minecraft mc = Minecraft.getInstance();
        //? if >= 26.2 {
        float yaw = mc.gameRenderer.mainCamera().yRot();
        //?} else if >= 1.21.11 {
        /*float yaw = mc.gameRenderer.getMainCamera().yRot();
        *///?} else {
        /*float yaw = mc.gameRenderer.getMainCamera().getYRot();
        *///?}
        long interval = zooming || yaw != lastYaw || moved(mc.getCameraEntity()) ? FRAME_NANOS : IDLE_NANOS;
        long now = System.nanoTime();
        long elapsed = now - lastRender;
        if (key == lastKey && elapsed < interval) return true;
        lastRender = key == lastKey && elapsed < interval * 2 ? lastRender + interval : now;
        lastKey = key;
        lastYaw = yaw;
        return false;
    }

    private static boolean moved(Entity camera) {
        return camera == null || camera.getX() != camera.xo || camera.getZ() != camera.zo;
    }
}
