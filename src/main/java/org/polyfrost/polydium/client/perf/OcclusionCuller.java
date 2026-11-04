package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {

//? if >= 26.3
import net.minecraft.client.Minecraft;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.polyfrost.polydium.mixin.client.perf.sodium.MixinOcclusionListAccessor;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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

    private static final boolean                            ENABLED         = !PolydiumMixinPlugin.isDisabled("OcclusionCull");
    private static final boolean                            FORCE           = Boolean.getBoolean("polydium.forceOcclusionCull");
    private static final int                                WIDTH           = OcclusionDepth.WIDTH;
    private static final int                                HEIGHT          = OcclusionDepth.HEIGHT;
    private static final int                                MASK_WORDS      = 4;
    private static final float                              NEAR            = 0.1F;
    private static final float                              CLIP_NEAR       = 0.01F;
    private static final float                              SCREEN_MARGIN   = 0.01F;
    private static final float                              DEPTH_RELATIVE  = 1.0e-3F;
    //? if >= 26.2 {
    private static final float                              DEPTH_ABSOLUTE  = 0.0F;
    //?} else {
    /*private static final float                              DEPTH_ABSOLUTE  = 1.0e-6F;
    *///?}
    private static final int                                LEVEL_SPAN      = 4;
    private static final int                                CORNER_SLOTS    = 256;
    private static final int                                CORNER_FLOATS   = 32;
    private static final int                                SIZE_BITS       = 6;
    private static final int                                MAX_CARVED      = 4096;
    private static final int                                CARVE_TILE      = 8;
    private static final int                                CARVE_COLUMNS   = (WIDTH + CARVE_TILE - 1) / CARVE_TILE;
    static final long                                       MIN_INTERVAL    = Integer.getInteger("polydium.occlusionCaptureMs", 100) * 1_000_000L;
    private static final double                             TEST_BUDGET     = 0.5;
    private static final double                             EYE_TOLERANCE   = 0.5;
    private static final double                             REPROJECT_LIMIT = 8.0;
    private static final float                              HIGH_YIELD      = 0.15F;
    private static final float                              LOW_YIELD       = 0.05F;
    private static final float                              YIELD_WEIGHT    = 0.25F;
    private static final int                                YIELD_SAMPLES   = 16;
    private static final int                                MAX_BACKOFF     = 16;
    private static final long                               BACKOFF_CEILING = 500_000_000L;
    private static final boolean                            CAN_BACK_OFF    = !PolydiumMixinPlugin.isDisabled("OcclusionBoundsSection") && !PolydiumMixinPlugin.isDisabled("OcclusionBoundsSprite") && !PolydiumMixinPlugin.isDisabled("OcclusionBoundsConsumer");
    private static final float[][]                          PYRAMID         = createPyramid();
    private static final int[]                              LEVEL_WIDTHS    = levelWidths();
    private static final float[]                            MATRIX          = new float[16];
    private static final float[]                            TESTED_DEPTH    = new float[WIDTH * HEIGHT];
    private static final float[]                            TESTED_MATRIX   = new float[16];
    private static final float[]                            CORNER_OFFSETS  = new float[CORNER_SLOTS * CORNER_FLOATS];
    private static final int[]                              CORNER_SIZES    = new int[CORNER_SLOTS];
    private static final float[]                            CLIP_CORNERS    = new float[8 * 3];
    private static final float[]                            CARVE_HIGHEST   = new float[CARVE_COLUMNS * ((HEIGHT + CARVE_TILE - 1) / CARVE_TILE)];
    private static final long[]                             HIDDEN          = new long[MASK_WORDS];
    private static final Matrix4f                           VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f                           INVERSE         = new Matrix4f();
    private static final ReferenceOpenHashSet<RenderRegion> REGIONS         = new ReferenceOpenHashSet<>();
    private static final ReferenceOpenHashSet<RenderRegion> CULLED_REGIONS  = new ReferenceOpenHashSet<>();
    private static final OcclusionSectionIterator            FILTERED        = new OcclusionSectionIterator();

    private static boolean culling;
    private static boolean sectionsChanged;
    private static boolean retestPending;
    private static int     testGeneration;
    private static int     testedRegionCount;
    private static int     testedCount;
    private static int     hiddenCount;
    private static int     captureBackoff = 1;
    private static float   yieldAverage   = -1.0F;
    private static long    testedVersion;
    private static long    excludedVersion;
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

        update(lists, matrices, cameraX, cameraY, cameraZ);

        if (OcclusionDepth.canCapture())
            collectAnimated(lists);
    }

    private static void update(ChunkRenderListIterable lists, ChunkRenderMatrices matrices, double cameraX, double cameraY, double cameraZ) {
        if (IrisShaders.packInUse()) {
            stopCulling();

            return;
        }

        //? if >= 26.3 {
        if (Minecraft.getInstance().gameRenderer.useImprovedTransparency()) {
            stopCulling();

            return;
        }
        //?}

        if (!FORCE && !GpuWaitMeter.isGpuBound()) {
            stopCulling();

            return;
        }

        //? if < 26.2 {
        /*for (int column = 0; column < 16; column += 4)
            MATRIX[column + 2] = (MATRIX[column + 3] - MATRIX[column + 2]) * 0.5F;
        *///?} else {
        if (matrices.projection().m22() > 0.5F) {
            for (int column = 0; column < 16; column += 4)
                MATRIX[column + 2] = (MATRIX[column + 3] + MATRIX[column + 2]) * 0.5F;
        }
        //?}
        collectSections(lists);
        releaseAbsentRegions();

        OcclusionDepth.describeFrame(MATRIX, cameraX, cameraY, cameraZ);

        if (OcclusionDepth.latestVersion() == 0L)
            return;

        if (isBeyond(cameraX, cameraY, cameraZ, OcclusionDepth.latestX(), OcclusionDepth.latestY(), OcclusionDepth.latestZ(), REPROJECT_LIMIT)) {
            showAll();

            return;
        }

        long    start      = System.nanoTime();
        boolean freshDepth = OcclusionDepth.latestVersion() != testedVersion;

        if (OcclusionDepth.latestVersion() != excludedVersion) {
            excludedVersion = OcclusionDepth.latestVersion();
            excludeOccluders();
        }

        if (isUnchanged(cameraX, cameraY, cameraZ))
            return;

        remember(cameraX, cameraY, cameraZ);
        buildPyramid();
        testSections(lists);
        culling       = true;
        retestPending = false;
        testNanos     = System.nanoTime() - start;

        if (freshDepth)
            trackYield();
    }

    static long captureInterval() {
        long interval = FORCE ? MIN_INTERVAL : Math.max(MIN_INTERVAL, (long) (testNanos / (GpuWaitMeter.waitShare() * TEST_BUDGET)));
        long ceiling  = Math.max(BACKOFF_CEILING, MIN_INTERVAL);

        return Math.min(Math.min(interval, ceiling) * captureBackoff, ceiling);
    }

    public static void reset() {
        stopCulling();
    }

    public static void requestRetest() {
        retestPending = true;
    }

    public static void occluderChanged(RenderRegion region, int section, int bounds, long drawnThrough) {
        int blockX = SectionPos.sectionToBlockCoord(region.getChunkX() + LocalSectionIndex.unpackX(section));
        int blockY = SectionPos.sectionToBlockCoord(region.getChunkY() + LocalSectionIndex.unpackY(section));
        int blockZ = SectionPos.sectionToBlockCoord(region.getChunkZ() + LocalSectionIndex.unpackZ(section));

        OcclusionDepth.excludeFromPending(blockX, blockY, blockZ, bounds, drawnThrough);

        if (OcclusionDepth.latestVersion() != 0L && OcclusionDepth.latestSequence() <= drawnThrough && clearDepth(blockX, blockY, blockZ, bounds, true, null))
            retestPending = true;
    }

    private static void excludeOccluders() {
        IntArrayList excluded = OcclusionDepth.latestExcluded();

        float[] merged = excluded.size() > MAX_CARVED * 4 ? CARVE_HIGHEST : null;

        if (merged != null)
            Arrays.fill(merged, Float.NEGATIVE_INFINITY);

        for (int index = 0; index < excluded.size(); index += 4)
            clearDepth(excluded.getInt(index), excluded.getInt(index + 1), excluded.getInt(index + 2), excluded.getInt(index + 3), false, merged);

        if (merged == null)
            return;

        float[] depths = OcclusionDepth.latestDepth();

        for (int index = 0; index < depths.length; index ++) {
            float depth = depths[index];

            if (depth > 0.0F && depth <= merged[index / WIDTH / CARVE_TILE * CARVE_COLUMNS + index % WIDTH / CARVE_TILE])
                depths[index] = 0.0F;
        }
    }

    private static void collectAnimated(ChunkRenderListIterable lists) {
        for (ChunkRenderList list : iterable(lists)) {
            RenderRegion    region    = list.getRegion();
            RegionOcclusion occlusion = (RegionOcclusion) region;
            int[]           animated  = occlusion.polydium$getAnimatedBounds();
            long[]          hidden    = occlusion.polydium$getHiddenSections();
            ByteIterator    sections  = list.sectionsWithGeometryIterator(false);

            if (sections == null)
                continue;

            while (sections.hasNext()) {
                int section = sections.nextByteAsInt();

                if (animated[section] != 0 && (hidden[section >>> 6] >>> section & 1L) == 0L)
                    OcclusionDepth.addFrameAnimated(
                        SectionPos.sectionToBlockCoord(region.getChunkX() + LocalSectionIndex.unpackX(section)),
                        SectionPos.sectionToBlockCoord(region.getChunkY() + LocalSectionIndex.unpackY(section)),
                        SectionPos.sectionToBlockCoord(region.getChunkZ() + LocalSectionIndex.unpackZ(section)),
                        animated[section]);
            }
        }
    }

    private static boolean clearDepth(int blockX, int blockY, int blockZ, int bounds, boolean rebuilt, float[] merged) {
        float[] matrix    = OcclusionDepth.latestMatrix();
        float   originX   = (float) (blockX - OcclusionDepth.latestX());
        float   originY   = (float) (blockY - OcclusionDepth.latestY());
        float   originZ   = (float) (blockZ - OcclusionDepth.latestZ());
        boolean crossing  = false;
        boolean ahead     = false;
        int     outside   = 0b1111;
        float   nearest   = Float.NEGATIVE_INFINITY;
        float   left      = Float.POSITIVE_INFINITY;
        float   bottom    = Float.POSITIVE_INFINITY;
        float   right     = Float.NEGATIVE_INFINITY;
        float   top       = Float.NEGATIVE_INFINITY;

        for (int corner = 0; corner < 8; corner ++) {
            float x     = originX + ((corner & 1) == 0 ? SectionBounds.min(bounds, 0) : SectionBounds.max(bounds, 0));
            float y     = originY + ((corner & 2) == 0 ? SectionBounds.min(bounds, 1) : SectionBounds.max(bounds, 1));
            float z     = originZ + ((corner & 4) == 0 ? SectionBounds.min(bounds, 2) : SectionBounds.max(bounds, 2));
            float clipX = matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12];
            float clipY = matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13];
            float w     = matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15];

            CLIP_CORNERS[corner * 3]     = clipX;
            CLIP_CORNERS[corner * 3 + 1] = clipY;
            CLIP_CORNERS[corner * 3 + 2] = w;

            ahead    |= w > 0.0F;
            crossing |= w <= NEAR;
            outside  &= (clipX < -w ? 1 : 0) | (clipX > w ? 2 : 0) | (clipY < -w ? 4 : 0) | (clipY > w ? 8 : 0);

            if (w <= CLIP_NEAR)
                continue;

            float inverseW = 1.0F / w;
            float screenX  = (clipX * inverseW * 0.5F + 0.5F) * WIDTH;
            float screenY  = (clipY * inverseW * 0.5F + 0.5F) * HEIGHT;
            float depth    = (matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14]) * inverseW;

            nearest = Math.max(nearest, depth);
            left    = Math.min(left, screenX);
            bottom  = Math.min(bottom, screenY);
            right   = Math.max(right, screenX);
            top     = Math.max(top, screenY);
        }

        if (!ahead || outside != 0)
            return false;

        if (crossing) {
            for (int corner = 0; corner < 8; corner ++) {
                for (int axis = 1; axis < 8; axis <<= 1) {
                    if ((corner & axis) != 0)
                        continue;

                    int   start = corner * 3;
                    int   end   = (corner | axis) * 3;
                    float from  = CLIP_CORNERS[start + 2];
                    float to    = CLIP_CORNERS[end + 2];

                    if (from <= CLIP_NEAR == to <= CLIP_NEAR)
                        continue;

                    float along   = (CLIP_NEAR - from) / (to - from);
                    float screenX = ((CLIP_CORNERS[start] + (CLIP_CORNERS[end] - CLIP_CORNERS[start]) * along) / CLIP_NEAR * 0.5F + 0.5F) * WIDTH;
                    float screenY = ((CLIP_CORNERS[start + 1] + (CLIP_CORNERS[end + 1] - CLIP_CORNERS[start + 1]) * along) / CLIP_NEAR * 0.5F + 0.5F) * HEIGHT;

                    left   = Math.min(left, screenX);
                    bottom = Math.min(bottom, screenY);
                    right  = Math.max(right, screenX);
                    top    = Math.max(top, screenY);
                }
            }

            if (left > right) {
                left   = 0.0F;
                bottom = 0.0F;
                right  = WIDTH;
                top    = HEIGHT;
            }

            nearest = Float.POSITIVE_INFINITY;
            left    = Math.max(left, 0.0F);
            bottom  = Math.max(bottom, 0.0F);
        }

        float[] depths    = OcclusionDepth.latestDepth();
        float   highest   = nearest * (1.0F + DEPTH_RELATIVE) + DEPTH_ABSOLUTE;
        int     pixelMinX = Math.max((int) (left - SCREEN_MARGIN) - 1, 0);
        int     pixelMinY = Math.max((int) (bottom - SCREEN_MARGIN) - 1, 0);
        int     pixelMaxX = Math.min((int) (right + SCREEN_MARGIN), WIDTH - 1);
        int     pixelMaxY = Math.min((int) (top + SCREEN_MARGIN), HEIGHT - 1);
        boolean cleared   = false;

        if (merged != null) {
            for (int y = pixelMinY / CARVE_TILE; y <= pixelMaxY / CARVE_TILE; y ++) {
                for (int x = pixelMinX / CARVE_TILE; x <= pixelMaxX / CARVE_TILE; x ++)
                    merged[y * CARVE_COLUMNS + x] = Math.max(merged[y * CARVE_COLUMNS + x], highest);
            }

            return false;
        }

        for (int y = pixelMinY; y <= pixelMaxY; y ++) {
            for (int x = pixelMinX; x <= pixelMaxX; x ++) {
                int   index = y * WIDTH + x;
                float depth = depths[index];

                if (depth > 0.0F && depth <= highest) {
                    depths[index] = 0.0F;
                    cleared       = true;
                }
            }
        }

        if (crossing && rebuilt && cleared)
            OcclusionDepth.requestCapture();

        return cleared;
    }

    private static void stopCulling() {
        showAll();
        REGIONS.clear();
        OcclusionDepth.reset();
        testedVersion   = 0L;
        excludedVersion = 0L;
        retestPending   = false;
        captureBackoff  = 1;
        yieldAverage    = -1.0F;
    }

    private static void trackYield() {
        if (!CAN_BACK_OFF || testedCount < YIELD_SAMPLES)
            return;

        float yield = (float) hiddenCount / testedCount;

        yieldAverage = yieldAverage < 0.0F ? yield : yieldAverage + (yield - yieldAverage) * YIELD_WEIGHT;

        if (yield >= HIGH_YIELD)
            captureBackoff = 1;
        else if (yieldAverage <= LOW_YIELD)
            captureBackoff = Math.min(captureBackoff << 1, MAX_BACKOFF);
        else
            captureBackoff = Math.max(captureBackoff >> 1, 1);
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

        if (retestPending)
            return false;

        if (OcclusionDepth.latestVersion() == testedVersion)
            return true;

        boolean identical = OcclusionDepth.latestX() == sourceX && OcclusionDepth.latestY() == sourceY && OcclusionDepth.latestZ() == sourceZ && Arrays.equals(OcclusionDepth.latestMatrix(), TESTED_MATRIX) && Arrays.equals(OcclusionDepth.latestDepth(), TESTED_DEPTH);

        if (identical) {
            testedVersion = OcclusionDepth.latestVersion();
            trackYield();
        }

        return identical;
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
        Arrays.fill(CORNER_SIZES, 0);
    }

    private static void testSections(ChunkRenderListIterable lists) {
        testedCount = 0;
        hiddenCount = 0;

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

                    testedCount ++;

                    if (isOccluded(region, section)) {
                        HIDDEN[section >>> 6] |= 1L << section;
                        hiddenCount ++;
                    }
                }
            }

            long[] current = occlusion.polydium$getHiddenSections();

            if (Arrays.equals(current, HIDDEN))
                continue;

            if ((HIDDEN[0] & ~current[0] | HIDDEN[1] & ~current[1] | HIDDEN[2] & ~current[2] | HIDDEN[3] & ~current[3]) != 0L)
                occlusion.polydium$setHiddenAfter(OcclusionDepth.captureCount());

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

        int bounds = (int) ((RegionOcclusion) region).polydium$getSectionBounds()[section];

        if (bounds == 0)
            bounds = SectionBounds.FULL;

        return isBoxOccluded((float) originX, (float) originY, (float) originZ, bounds);
    }

    private static int placeCorners(int bounds) {
        int sizes = 1 << SIZE_BITS * 3;

        for (int axis = 0; axis < 3; axis ++)
            sizes |= SectionBounds.max(bounds, axis) - SectionBounds.min(bounds, axis) << axis * SIZE_BITS;

        int slot  = HashCommon.mix(sizes) & CORNER_SLOTS - 1;
        int start = slot * CORNER_FLOATS;

        if (CORNER_SIZES[slot] == sizes)
            return start;

        CORNER_SIZES[slot] = sizes;

        for (int axis = 0; axis < 3; axis ++) {
            int size  = SectionBounds.max(bounds, axis) - SectionBounds.min(bounds, axis);
            int count = 1 << axis;

            for (int corner = 0; corner < count; corner ++) {
                for (int row = 0; row < 4; row ++)
                    CORNER_OFFSETS[start + (corner + count) * 4 + row] = CORNER_OFFSETS[start + corner * 4 + row] + TESTED_MATRIX[axis * 4 + row] * size;
            }
        }

        return start;
    }

    private static boolean isBoxOccluded(float originX, float originY, float originZ, int bounds) {
        originX += SectionBounds.min(bounds, 0);
        originY += SectionBounds.min(bounds, 1);
        originZ += SectionBounds.min(bounds, 2);

        int     start   = placeCorners(bounds);
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

        for (int offset = start; offset < start + CORNER_FLOATS; offset += 4) {
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
