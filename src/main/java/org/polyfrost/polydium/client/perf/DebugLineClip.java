package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import org.joml.Math;
import org.joml.Matrix4fc;

public final class DebugLineClip {
    private static final float NEAR_VIEW_Z = -0.05F;
    private static final float MIN_DENOMINATOR = 1.0E-9F;

    private DebugLineClip() {
    }

    public static float viewZ(Matrix4fc m, int properties, float x, float y, float z) {
        if ((properties & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            return z;
        }
        if ((properties & Matrix4fc.PROPERTY_TRANSLATION) != 0) {
            return Math.fma(m.m32(), 1.0F, z);
        }
        return Math.fma(m.m02(), x, Math.fma(m.m12(), y, Math.fma(m.m22(), z, m.m32() * 1.0F)));
    }

    public static boolean clip(Matrix4fc modelView, float[] line) {
        int properties = modelView.properties();
        float startDepth = viewZ(modelView, properties, line[0], line[1], line[2]);
        float endDepth = viewZ(modelView, properties, line[3], line[4], line[5]);
        boolean startBehind = startDepth > NEAR_VIEW_Z;
        boolean endBehind = endDepth > NEAR_VIEW_Z;
        if (startBehind && endBehind) {
            return false;
        }
        if (startBehind || endBehind) {
            float denominator = endDepth - startDepth;
            if (java.lang.Math.abs(denominator) < MIN_DENOMINATOR) {
                return false;
            }
            float t = (NEAR_VIEW_Z - startDepth) / denominator;
            t = t < 0.0F ? 0.0F : java.lang.Math.min(t, 1.0F);
            float x = Math.fma(line[3] - line[0], t, line[0]);
            float y = Math.fma(line[4] - line[1], t, line[1]);
            float z = Math.fma(line[5] - line[2], t, line[2]);
            int offset = startBehind ? 0 : 3;
            line[offset] = x;
            line[offset + 1] = y;
            line[offset + 2] = z;
        }
        return true;
    }

    public static boolean identityExact(float[] line) {
        for (int i = 0; i < 6; i++) {
            int bits = Float.floatToRawIntBits(line[i]);
            if (bits == 0x80000000 || (bits & 0x7FFFFFFF) >= 0x7F800000) {
                return false;
            }
        }
        return true;
    }
}
//?}
