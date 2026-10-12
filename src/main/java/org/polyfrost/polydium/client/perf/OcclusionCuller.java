package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {

import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.polyfrost.polydium.mixin.client.perf.sodium.MixinOcclusionListAccessor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Arrays;
import java.util.Iterator;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.LocalSectionIndex;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import net.minecraft.core.SectionPos;
import org.joml.Matrix4f;

public final class OcclusionCuller {

    private static final boolean                            ENABLED         = PolydiumMixinPlugin.isEnabled("OcclusionCull");
    private static final boolean                            FORCE           = Boolean.getBoolean("polydium.forceOcclusionCull");
    private static final int                                WIDTH           = OcclusionDepth.WIDTH;
    private static final int                                HEIGHT          = OcclusionDepth.HEIGHT;
    private static final int                                MASK_WORDS      = 4;
    private static final float                              SECTION_SIZE    = 16.0F;
    private static final float                              NEAR            = 0.1F;
    private static final float                              SCREEN_MARGIN   = 0.01F;
    private static final float                              DEPTH_RELATIVE  = 1.0e-3F;
    //? if >= 26.2 {
    private static final float                              DEPTH_ABSOLUTE  = 0.0F;
    //?} else {
    /*private static final float                              DEPTH_ABSOLUTE  = 1.0e-6F;
    *///?}
    private static final int                                LEVEL_SPAN      = 4;
    private static final long                               MIN_INTERVAL    = 4_000_000L;
    private static final double                             TEST_BUDGET     = 0.5;
    private static final double                             EYE_TOLERANCE   = 0.5;
    private static final double                             REPROJECT_LIMIT = 8.0;
    private static final float[][]                          PYRAMID         = createPyramid();
    private static final int[]                              LEVEL_WIDTHS    = levelWidths();
    private static final float[]                            MATRIX          = new float[16];
    private static final float[]                            TESTED_DEPTH    = new float[WIDTH * HEIGHT];
    private static final float[]                            TESTED_MATRIX   = new float[16];
    private static final float[]                            CORNER_OFFSETS  = new float[32];
    private static final long[]                             HIDDEN          = new long[MASK_WORDS];
    private static final Matrix4f                           VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f                           INVERSE         = new Matrix4f();
    private static final ReferenceOpenHashSet<RenderRegion> REGIONS         = new ReferenceOpenHashSet<>();
    private static final ReferenceOpenHashSet<RenderRegion> CULLED_REGIONS  = new ReferenceOpenHashSet<>();
    private static final OcclusionSectionIterator            FILTERED        = new OcclusionSectionIterator();

    private static boolean culling;
    private static boolean sectionsChanged;
    private static int     testGeneration;
    private static int     testedRegionCount;
    private static long    testedVersion;
    private static long    testNanos;
    private static double  testedX;
    private static double  testedY;
    private static double  testedZ;
    private static double  sourceX;
    private static double  sourceY;
    private static double  sourceZ;

    private OcclusionCuller() {
    }

    public static ByteIterator filter(ChunkRenderList list, ByteIterator sections) {
        if (sections == null || !CULLED_REGIONS.contains(list.getRegion()))
            return sections;

        long[] hidden = ((RegionOcclusion) list.getRegion()).polydium$getHiddenSections();

        FILTERED.reset();

        while (sections.hasNext()) {
            int section = sections.nextByteAsInt();

            if ((hidden[section >>> 6] >>> section & 1L) == 0L)
                FILTERED.add(section);
        }

        return FILTERED.isEmpty() ? null : FILTERED;
    }

    public static void cull(ChunkRenderListIterable lists, ChunkRenderMatrices matrices, double cameraX, double cameraY, double cameraZ) {
        VIEW_PROJECTION.set(matrices.projection()).mul(matrices.modelView()).get(MATRIX);

        if (!ENABLED)
            return;

        //? if < 26.2 {
        /*for (int column = 0; column < 16; column += 4)
            MATRIX[column + 2] = (MATRIX[column + 3] - MATRIX[column + 2]) * 0.5F;
        *///?}
        collectSections(lists);
        releaseAbsentRegions();

        if (IrisShaders.packInUse() || !FORCE && !GpuWaitMeter.isGpuBound()) {
            stopCulling();

            return;
        }

        OcclusionDepth.describeFrame(MATRIX, cameraX, cameraY, cameraZ);

        if (OcclusionDepth.latestVersion() == 0L)
            return;

        if (isBeyond(cameraX, cameraY, cameraZ, OcclusionDepth.latestX(), OcclusionDepth.latestY(), OcclusionDepth.latestZ(), REPROJECT_LIMIT)) {
            showAll();

            return;
        }

        if (isUnchanged(cameraX, cameraY, cameraZ))
            return;

        long start = System.nanoTime();

        remember(cameraX, cameraY, cameraZ);
        buildPyramid();
        testSections(lists);
        culling   = true;
        testNanos = System.nanoTime() - start;
    }

    static long captureInterval() {
        if (FORCE)
            return MIN_INTERVAL;

        return Math.max(MIN_INTERVAL, (long) (testNanos / (GpuWaitMeter.waitShare() * TEST_BUDGET)));
    }

    public static void reset() {
        stopCulling();
    }

    private static void stopCulling() {
        showAll();
        OcclusionDepth.reset();
    }

    private static void showAll() {
        for (RenderRegion region : CULLED_REGIONS)
            clearHidden(region);

        CULLED_REGIONS.clear();
        culling = false;
    }

    private static boolean isBeyond(double x, double y, double z, double originX, double originY, double originZ, double limit) {
        double moveX = x - originX;
        double moveY = y - originY;
        double moveZ = z - originZ;

        return moveX * moveX + moveY * moveY + moveZ * moveZ > limit * limit;
    }

    private static void collectSections(ChunkRenderListIterable lists) {
        REGIONS.clear();
        sectionsChanged = false;

        for (ChunkRenderList list : iterable(lists)) {
            RenderRegion    region    = list.getRegion();
            RegionOcclusion occlusion = (RegionOcclusion) region;

            REGIONS.add(region);

            if (!sectionsChanged && (occlusion.polydium$getTestedGeneration() != testGeneration || !Arrays.equals(((MixinOcclusionListAccessor) list).polydium$sectionsWithGeometryMap(), occlusion.polydium$getTestedSections())))
                sectionsChanged = true;
        }

        if (REGIONS.size() != testedRegionCount)
            sectionsChanged = true;
    }

    private static void releaseAbsentRegions() {
        Iterator<RenderRegion> iterator = CULLED_REGIONS.iterator();

        while (iterator.hasNext()) {
            RenderRegion region = iterator.next();

            if (REGIONS.contains(region))
                continue;

            clearHidden(region);
            iterator.remove();
        }
    }

    private static void clearHidden(RenderRegion region) {
        Arrays.fill(((RegionOcclusion) region).polydium$getHiddenSections(), 0L);
        region.clearAllCachedBatches();
    }

    private static boolean isUnchanged(double cameraX, double cameraY, double cameraZ) {
        if (!culling || sectionsChanged || isBeyond(cameraX, cameraY, cameraZ, testedX, testedY, testedZ, EYE_TOLERANCE))
            return false;

        if (OcclusionDepth.latestVersion() == testedVersion)
            return true;

        return OcclusionDepth.latestX() == sourceX && OcclusionDepth.latestY() == sourceY && OcclusionDepth.latestZ() == sourceZ && Arrays.equals(OcclusionDepth.latestMatrix(), TESTED_MATRIX) && Arrays.equals(OcclusionDepth.latestDepth(), TESTED_DEPTH);
    }

    private static void remember(double cameraX, double cameraY, double cameraZ) {
        testedVersion = OcclusionDepth.latestVersion();
        sourceX       = OcclusionDepth.latestX();
        sourceY       = OcclusionDepth.latestY();
        sourceZ       = OcclusionDepth.latestZ();
        testedX       = cameraX;
        testedY       = cameraY;
        testedZ       = cameraZ;

        System.arraycopy(OcclusionDepth.latestMatrix(), 0, TESTED_MATRIX, 0, TESTED_MATRIX.length);
        System.arraycopy(OcclusionDepth.latestDepth(), 0, TESTED_DEPTH, 0, TESTED_DEPTH.length);
        testGeneration ++;
        testedRegionCount = REGIONS.size();

        for (int corner = 0; corner < 8; corner ++) {
            float offsetX = (corner & 1) == 0 ? 0.0F : SECTION_SIZE;
            float offsetY = (corner & 2) == 0 ? 0.0F : SECTION_SIZE;
            float offsetZ = (corner & 4) == 0 ? 0.0F : SECTION_SIZE;

            for (int row = 0; row < 4; row ++)
                CORNER_OFFSETS[corner * 4 + row] = TESTED_MATRIX[row] * offsetX + TESTED_MATRIX[4 + row] * offsetY + TESTED_MATRIX[8 + row] * offsetZ;
        }
    }

    private static void testSections(ChunkRenderListIterable lists) {
        for (ChunkRenderList list : iterable(lists)) {
            RenderRegion    region    = list.getRegion();
            RegionOcclusion occlusion = (RegionOcclusion) region;
            ByteIterator    sections  = list.sectionsWithGeometryIterator(false);

            System.arraycopy(((MixinOcclusionListAccessor) list).polydium$sectionsWithGeometryMap(), 0, occlusion.polydium$getTestedSections(), 0, MASK_WORDS);
            occlusion.polydium$setTestedGeneration(testGeneration);
            Arrays.fill(HIDDEN, 0L);

            if (sections != null) {
                while (sections.hasNext()) {
                    int section = sections.nextByteAsInt();

                    if (isOccluded(region, section))
                        HIDDEN[section >>> 6] |= 1L << section;
                }
            }

            long[] current = occlusion.polydium$getHiddenSections();

            if (Arrays.equals(current, HIDDEN))
                continue;

            System.arraycopy(HIDDEN, 0, current, 0, MASK_WORDS);
            region.clearAllCachedBatches();

            if ((HIDDEN[0] | HIDDEN[1] | HIDDEN[2] | HIDDEN[3]) == 0L)
                CULLED_REGIONS.remove(region);
            else
                CULLED_REGIONS.add(region);
        }
    }

    private static boolean isOccluded(RenderRegion region, int section) {
        double originX = SectionPos.sectionToBlockCoord(region.getChunkX() + LocalSectionIndex.unpackX(section)) - testedX;
        double originY = SectionPos.sectionToBlockCoord(region.getChunkY() + LocalSectionIndex.unpackY(section)) - testedY;
        double originZ = SectionPos.sectionToBlockCoord(region.getChunkZ() + LocalSectionIndex.unpackZ(section)) - testedZ;

        return isCubeOccluded((float) originX, (float) originY, (float) originZ);
    }

    private static boolean isCubeOccluded(float originX, float originY, float originZ) {
        float[] matrix  = TESTED_MATRIX;
        float   baseX   = matrix[0] * originX + matrix[4] * originY + matrix[8] * originZ + matrix[12];
        float   baseY   = matrix[1] * originX + matrix[5] * originY + matrix[9] * originZ + matrix[13];
        float   baseZ   = matrix[2] * originX + matrix[6] * originY + matrix[10] * originZ + matrix[14];
        float   baseW   = matrix[3] * originX + matrix[7] * originY + matrix[11] * originZ + matrix[15];
        float   nearest = Float.NEGATIVE_INFINITY;
        float   left    = Float.POSITIVE_INFINITY;
        float   bottom  = Float.POSITIVE_INFINITY;
        float   right   = Float.NEGATIVE_INFINITY;
        float   top     = Float.NEGATIVE_INFINITY;

        for (int offset = 0; offset < CORNER_OFFSETS.length; offset += 4) {
            float w = baseW + CORNER_OFFSETS[offset + 3];

            if (w <= NEAR)
                return false;

            float inverseW = 1.0F / w;
            float screenX  = ((baseX + CORNER_OFFSETS[offset]) * inverseW * 0.5F + 0.5F) * WIDTH;
            float screenY  = ((baseY + CORNER_OFFSETS[offset + 1]) * inverseW * 0.5F + 0.5F) * HEIGHT;

            nearest = Math.max(nearest, (baseZ + CORNER_OFFSETS[offset + 2]) * inverseW);
            left    = Math.min(left, screenX);
            bottom  = Math.min(bottom, screenY);
            right   = Math.max(right, screenX);
            top     = Math.max(top, screenY);
        }

        return isRectOccluded(nearest, left, bottom, right, top);
    }

    private static boolean isRectOccluded(float nearest, float left, float bottom, float right, float top) {
        if (left - SCREEN_MARGIN < 0.0F || bottom - SCREEN_MARGIN < 0.0F || right + SCREEN_MARGIN >= WIDTH || top + SCREEN_MARGIN >= HEIGHT)
            return false;

        int pixelMinX = Math.max((int) (left - SCREEN_MARGIN), 0);
        int pixelMinY = Math.max((int) (bottom - SCREEN_MARGIN), 0);
        int pixelMaxX = Math.min((int) (right + SCREEN_MARGIN), WIDTH - 1);
        int pixelMaxY = Math.min((int) (top + SCREEN_MARGIN), HEIGHT - 1);
        int level     = 0;

        while ((pixelMaxX >> level) - (pixelMinX >> level) >= LEVEL_SPAN || (pixelMaxY >> level) - (pixelMinY >> level) >= LEVEL_SPAN)
            level ++;

        float[] depths     = PYRAMID[level];
        int     levelWidth = LEVEL_WIDTHS[level];
        float   threshold  = nearest * (1.0F + DEPTH_RELATIVE) + DEPTH_ABSOLUTE;

        for (int y = pixelMinY >> level; y <= pixelMaxY >> level; y ++) {
            for (int x = pixelMinX >> level; x <= pixelMaxX >> level; x ++) {
                if (depths[y * levelWidth + x] <= threshold)
                    return false;
            }
        }

        return true;
    }

    private static void reproject() {
        float[] target = PYRAMID[0];
        float   moveX  = (float) (testedX - sourceX);
        float   moveY  = (float) (testedY - sourceY);
        float   moveZ  = (float) (testedZ - sourceZ);

        if (moveX == 0.0F && moveY == 0.0F && moveZ == 0.0F) {
            System.arraycopy(TESTED_DEPTH, 0, target, 0, TESTED_DEPTH.length);

            return;
        }

        float[] matrix   = TESTED_MATRIX;
        float   shiftX   = matrix[0] * moveX + matrix[4] * moveY + matrix[8] * moveZ;
        float   shiftY   = matrix[1] * moveX + matrix[5] * moveY + matrix[9] * moveZ;
        float   shiftZ   = matrix[2] * moveX + matrix[6] * moveY + matrix[10] * moveZ;
        float   shiftW   = matrix[3] * moveX + matrix[7] * moveY + matrix[11] * moveZ;

        INVERSE.set(matrix).invert();

        float   inverseX = INVERSE.m03();
        float   inverseY = INVERSE.m13();
        float   inverseZ = INVERSE.m23();
        float   inverseW = INVERSE.m33();

        Arrays.fill(target, Float.POSITIVE_INFINITY);

        for (int y = 0; y < HEIGHT; y ++) {
            float ndcY = (y + 0.5F) / HEIGHT * 2.0F - 1.0F;

            for (int x = 0; x < WIDTH; x ++) {
                int   index = y * WIDTH + x;
                float depth = TESTED_DEPTH[index];

                if (depth <= 0.0F) {
                    target[index] = 0.0F;

                    continue;
                }

                float ndcX  = (x + 0.5F) / WIDTH * 2.0F - 1.0F;
                float scale = inverseX * ndcX + inverseY * ndcY + inverseZ * depth + inverseW;

                if (scale <= 0.0F)
                    continue;

                float w     = 1.0F / scale;
                float clipW = w - shiftW;

                if (clipW <= NEAR)
                    continue;

                float inverseClipW = 1.0F / clipW;
                float screenX      = ((ndcX * w - shiftX) * inverseClipW * 0.5F + 0.5F) * WIDTH;
                float screenY      = ((ndcY * w - shiftY) * inverseClipW * 0.5F + 0.5F) * HEIGHT;
                float moved        = (depth * w - shiftZ) * inverseClipW;
                int   minX         = Math.max((int) Math.floor(screenX - 0.5F), 0);
                int   minY         = Math.max((int) Math.floor(screenY - 0.5F), 0);
                int   maxX         = Math.min((int) Math.ceil(screenX + 0.5F) - 1, WIDTH - 1);
                int   maxY         = Math.min((int) Math.ceil(screenY + 0.5F) - 1, HEIGHT - 1);

                for (int targetY = minY; targetY <= maxY; targetY ++) {
                    for (int targetX = minX; targetX <= maxX; targetX ++)
                        target[targetY * WIDTH + targetX] = Math.min(target[targetY * WIDTH + targetX], moved);
                }
            }
        }

        for (int index = 0; index < target.length; index ++) {
            if (target[index] == Float.POSITIVE_INFINITY)
                target[index] = 0.0F;
        }
    }

    private static void buildPyramid() {
        reproject();

        int width  = WIDTH;
        int height = HEIGHT;

        for (int level = 1; level < PYRAMID.length; level ++) {
            float[] source       = PYRAMID[level - 1];
            float[] target       = PYRAMID[level];
            int     targetWidth  = (width + 1) >> 1;
            int     targetHeight = (height + 1) >> 1;

            for (int y = 0; y < targetHeight; y ++) {
                int sourceY0 = y << 1;
                int sourceY1 = Math.min(sourceY0 + 1, height - 1);

                for (int x = 0; x < targetWidth; x ++) {
                    int sourceX0 = x << 1;
                    int sourceX1 = Math.min(sourceX0 + 1, width - 1);

                    target[y * targetWidth + x] = Math.min(Math.min(source[sourceY0 * width + sourceX0], source[sourceY0 * width + sourceX1]), Math.min(source[sourceY1 * width + sourceX0], source[sourceY1 * width + sourceX1]));
                }
            }

            width  = targetWidth;
            height = targetHeight;
        }
    }

    private static Iterable<ChunkRenderList> iterable(ChunkRenderListIterable lists) {
        return () -> lists.iterator(false);
    }

    private static int[] levelWidths() {
        int[] widths = new int[PYRAMID.length];
        int   width  = WIDTH;

        for (int level = 0; level < widths.length; level ++) {
            widths[level] = width;
            width         = (width + 1) >> 1;
        }

        return widths;
    }

    private static float[][] createPyramid() {
        int levels = 1;
        int width  = WIDTH;
        int height = HEIGHT;

        while (width > 1 || height > 1) {
            width  = (width + 1) >> 1;
            height = (height + 1) >> 1;
            levels ++;
        }

        float[][] pyramid = new float[levels][];

        width  = WIDTH;
        height = HEIGHT;

        for (int level = 0; level < levels; level ++) {
            pyramid[level] = new float[width * height];
            width          = (width + 1) >> 1;
            height         = (height + 1) >> 1;
        }

        return pyramid;
    }
}
//?}
