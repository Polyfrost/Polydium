package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Method;

public final class ParticleQuadSupport {
    private static final String SODIUM_MIXINS = "net.caffeinemc.mods.sodium.";

    private static final float[] CORNER_X = {1.0F, 1.0F, -1.0F, -1.0F};
    private static final float[] CORNER_Y = {-1.0F, 1.0F, 1.0F, -1.0F};

    private static final float[] ROTATED = new float[12];
    private static final Vector3f SCRATCH = new Vector3f();
    private static final Quaternionf ROTATION = new Quaternionf();
    private static boolean cached;
    private static int cachedX, cachedY, cachedZ, cachedW;

    private static int batchState;
    private static int directState;

    private ParticleQuadSupport() {
    }

    public static float[] rotatedCorners(float x, float y, float z, float w) {
        int bx = Float.floatToRawIntBits(x);
        int by = Float.floatToRawIntBits(y);
        int bz = Float.floatToRawIntBits(z);
        int bw = Float.floatToRawIntBits(w);
        if (cached && bx == cachedX && by == cachedY && bz == cachedZ && bw == cachedW) {
            return ROTATED;
        }

        ROTATION.set(x, y, z, w);
        for (int corner = 0; corner < 4; corner++) {
            SCRATCH.set(CORNER_X[corner], CORNER_Y[corner], 0.0F).rotate(ROTATION);
            ROTATED[corner * 3] = SCRATCH.x;
            ROTATED[corner * 3 + 1] = SCRATCH.y;
            ROTATED[corner * 3 + 2] = SCRATCH.z;
        }

        cached = true;
        cachedX = bx;
        cachedY = by;
        cachedZ = bz;
        cachedW = bw;
        return ROTATED;
    }

    public static boolean canBatch(Class<?> renderState, Class<?> storage) {
        if (batchState == 0) {
            batchState = scan(renderState, storage) ? 1 : -1;
        }
        return batchState > 0;
    }

    public static boolean canWriteDirect(Class<?> bufferBuilder) {
        if (directState == 0) {
            directState = scan(bufferBuilder) ? 1 : -1;
        }
        return directState > 0;
    }

    private static boolean scan(Class<?>... classes) {
        boolean sodium = false;
        try {
            for (Class<?> type : classes) {
                for (Method method : type.getDeclaredMethods()) {
                    MixinMerged merged = method.getAnnotation(MixinMerged.class);
                    if (merged == null) {
                        continue;
                    }
                    String mixin = merged.mixin();
                    if (mixin.startsWith(SODIUM_MIXINS)) {
                        sodium = true;
                    } else if (!DirectVertexBuffers.harmless(mixin, method)) {
                        LoggerFactory.getLogger("polydium/perf").info("Particle fast path disabled: {} is modified by {}", type.getName(), mixin);
                        return false;
                    }
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return sodium;
    }
}
//?}
