package org.polyfrost.polydium.client.perf;

import org.joml.Matrix4f;

public final class SinglePassTextShadow {
    private static Matrix4f pendingPose;

    public static void offer(Matrix4f pose) {
        pendingPose = pose;
    }

    public static Matrix4f take() {
        Matrix4f pose = pendingPose;
        pendingPose = null;
        return pose;
    }
}
