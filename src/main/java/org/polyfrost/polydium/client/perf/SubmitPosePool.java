package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

public final class SubmitPosePool {
    private static final int CAPACITY = 4096;
    private static final PoseStack.Pose[] POSES = new PoseStack.Pose[CAPACITY];
    private static int next;

    private SubmitPosePool() {
    }

    public static PoseStack.Pose copy(PoseStack.Pose source) {
        if (next == CAPACITY || !RenderSystem.isOnRenderThread()) {
            return source.copy();
        }
        PoseStack.Pose pose = POSES[next];
        if (pose == null) {
            POSES[next] = source.copy();
            return POSES[next++];
        }
        next++;
        pose.set(source);
        return pose;
    }

    public static void recycle() {
        next = 0;
    }
}
//?}
