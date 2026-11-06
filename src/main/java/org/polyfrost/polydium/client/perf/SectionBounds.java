package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {

import it.unimi.dsi.fastutil.HashCommon;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class SectionBounds {

    private static final int                 ORIGIN   = 8;
    private static final int                 MAX_BITS = 31;
    private static final int                 BITS     = 5;
    private static final long                MIX      = 0x9E3779B97F4A7C15L;
    private static final ThreadLocal<Tracked> TRACKED = ThreadLocal.withInitial(Tracked::new);

    public static final int FULL = pack(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F);

    private static final int UNTRACKED = pack(-ORIGIN, -ORIGIN, -ORIGIN, MAX_BITS + 1 - ORIGIN, MAX_BITS + 1 - ORIGIN, MAX_BITS + 1 - ORIGIN);

    private SectionBounds() {
    }

    public static Tracked tracked() {
        return TRACKED.get();
    }

    public static void include(Tracked tracked, ChunkVertexEncoder.Vertex[] vertices, int materialBits, int pass, boolean translucent) {
        float[] extent = tracked.extent;
        float   minX   = extent[0];
        float   minY   = extent[1];
        float   minZ   = extent[2];
        float   maxX   = extent[3];
        float   maxY   = extent[4];
        float   maxZ   = extent[5];
        long    hash   = pass;

        for (ChunkVertexEncoder.Vertex vertex : vertices) {
            float x = vertex.x;
            float y = vertex.y;
            float z = vertex.z;

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;

            if (translucent)
                continue;

            hash = (hash ^ ((long) Float.floatToRawIntBits(x) << 32 | Integer.toUnsignedLong(Float.floatToRawIntBits(y)))) * MIX;
            hash = (hash ^ ((long) Float.floatToRawIntBits(z) << 32 | Integer.toUnsignedLong(Float.floatToRawIntBits(vertex.u)))) * MIX;
            hash = (hash ^ ((long) Float.floatToRawIntBits(vertex.v) << 32 | Integer.toUnsignedLong(vertex.color & 0xFF000000 | materialBits))) * MIX;
        }

        extent[0] = minX;
        extent[1] = minY;
        extent[2] = minZ;
        extent[3] = maxX;
        extent[4] = maxY;
        extent[5] = maxZ;

        if (!translucent)
            tracked.shape += HashCommon.murmurHash3(hash);

        if (tracked.animatedAhead) {
            tracked.animatedAhead = false;

            if (!translucent)
                tracked.includeAnimated(vertices);
        }

        tracked.lastOpaque = translucent ? null : vertices;
    }

    // For renderers that push a quad before adding its sprite.
    public static void spritePushed(TextureAtlasSprite sprite, Material material) {
        if (!isAnimatedCutout(sprite, material))
            return;

        Tracked tracked = TRACKED.get();

        if (tracked.lastOpaque != null)
            tracked.includeAnimated(tracked.lastOpaque);
    }

    public static void spriteAhead(TextureAtlasSprite sprite, Material material) {
        if (isAnimatedCutout(sprite, material))
            TRACKED.get().animatedAhead = true;
    }

    public static void quadDiscarded() {
        TRACKED.get().animatedAhead = false;
    }

    private static boolean isAnimatedCutout(TextureAtlasSprite sprite, Material material) {
        return (material.isTranslucent() || material.alphaCutoff != AlphaCutoffParameter.ZERO) && SpriteUtil.INSTANCE.hasAnimation(sprite);
    }

    public static int takeAnimated() {
        Tracked tracked = TRACKED.get();

        if (tracked.decided && !tracked.active)
            return UNTRACKED;

        float[] animated = tracked.animated;

        return animated[0] > animated[3] ? 0 : pack(animated[0], animated[1], animated[2], animated[3], animated[4], animated[5]);
    }

    public static long take() {
        Tracked tracked = TRACKED.get();
        float[] extent  = tracked.extent;
        long    packed  = tracked.decided && !tracked.active ? UNTRACKED : extent[0] > extent[3] ? 0L : (tracked.shape ^ tracked.shape >>> Integer.SIZE) << Integer.SIZE | pack(extent[0], extent[1], extent[2], extent[3], extent[4], extent[5]);

        tracked.reset();

        return packed;
    }

    public static int min(int bounds, int axis) {
        return (bounds >>> axis * BITS & MAX_BITS) - ORIGIN;
    }

    public static int max(int bounds, int axis) {
        return (bounds >>> (axis + 3) * BITS & MAX_BITS) - ORIGIN + 1;
    }

    private static int pack(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return lower(minX) | lower(minY) << BITS | lower(minZ) << BITS * 2 | upper(maxX) << BITS * 3 | upper(maxY) << BITS * 4 | upper(maxZ) << BITS * 5;
    }

    private static int lower(float value) {
        return Math.clamp((int) Math.floor(value) + ORIGIN, 0, MAX_BITS);
    }

    private static int upper(float value) {
        return Math.clamp((int) Math.ceil(value) + ORIGIN - 1, 1, MAX_BITS);
    }

    public static final class Tracked {

        private final float[]               extent   = new float[6];
        private final float[]               animated = new float[6];
        private ChunkVertexEncoder.Vertex[] lastOpaque;
        private long                        shape;
        private boolean                     animatedAhead;
        private boolean                     decided;
        private boolean                     active;

        private Tracked() {
            this.reset();
        }

        public boolean active() {
            if (!this.decided) {
                this.decided = true;
                this.active  = !IrisShaders.packInUse();
            }

            return this.active;
        }

        private void includeAnimated(ChunkVertexEncoder.Vertex[] vertices) {
            for (ChunkVertexEncoder.Vertex vertex : vertices) {
                this.animated[0] = Math.min(this.animated[0], vertex.x);
                this.animated[1] = Math.min(this.animated[1], vertex.y);
                this.animated[2] = Math.min(this.animated[2], vertex.z);
                this.animated[3] = Math.max(this.animated[3], vertex.x);
                this.animated[4] = Math.max(this.animated[4], vertex.y);
                this.animated[5] = Math.max(this.animated[5], vertex.z);
            }
        }

        private void reset() {
            this.shape         = 0;
            this.lastOpaque    = null;
            this.animatedAhead = false;
            this.decided       = false;

            for (int axis = 0; axis < 3; axis ++) {
                this.extent[axis]       = Float.POSITIVE_INFINITY;
                this.extent[axis + 3]   = Float.NEGATIVE_INFINITY;
                this.animated[axis]     = Float.POSITIVE_INFINITY;
                this.animated[axis + 3] = Float.NEGATIVE_INFINITY;
            }
        }
    }

    public interface Holder {

        long polydium$getBounds();

        int polydium$getAnimatedBounds();
    }

    public interface PassHolder {

        void polydium$setPass(int pass, boolean translucent);
    }
}
//?}
