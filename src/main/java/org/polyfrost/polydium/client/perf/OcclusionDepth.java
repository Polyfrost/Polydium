//~ identifier
package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >= 26.2 {
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
//?} else {
/*import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.OptionalInt;
*///?}
//? if >= 26.3 {
import com.mojang.renderpearl.api.pipeline.UniformType;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
//?}
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class OcclusionDepth {

    public static final int WIDTH  = 320;
    public static final int HEIGHT = 180;

    private static final int             TEXELS          = WIDTH * HEIGHT;
    private static final int             SLOTS           = 3;
    private static final int             STILL_INTERVAL  = 8;
    private static final int             TEXTURE_USAGE   = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_SRC;
    private static final int             BUFFER_USAGE    = GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST;
    //? if >= 26.3 {
    private static final int             PUSH_CONSTANTS  = 16;
    private static final int             WRITE_RED       = 1;
    private static final BindGroupLayout SAMPLER         = BindGroupLayout.builder().withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER).build();
    private static final RenderPipeline  PIPELINE        = pipelineBuilder().withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/occlusion_depth")).build();
    private static final RenderPipeline  FETCH_PIPELINE  = pipelineBuilder().withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/occlusion_depth_fetch")).withShaderDefine("TEXEL_FETCH").build();
    //?} else if >= 26.2 {
    /*private static final RenderPipeline  PIPELINE        = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/occlusion_depth"))
        .withBindGroupLayout(BindGroupLayout.builder().withSampler("DepthSampler").build())
        .withCull(false)
        .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth_gl"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth_gl"))
        .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.R32_FLOAT, ColorTargetState.WRITE_RED))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
    *///?} else {
    /*private static final RenderPipeline  PIPELINE        = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/occlusion_depth"))
        .withSampler("DepthSampler")
        .withShaderDefine("PACKED_DEPTH")
        .withCull(false)
        .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth_gl"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth_gl"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .build();
    *///?}
    private static final Slot[]          RING            = new Slot[SLOTS];
    private static final float[]         FRAME_MATRIX    = new float[16];
    private static final float[]         CAPTURED_MATRIX = new float[16];
    private static final float[]         LATEST_DEPTH    = new float[TEXELS];
    private static final float[]         LATEST_MATRIX   = new float[16];
    private static final IntArrayList    FRAME_ANIMATED  = new IntArrayList();
    private static final IntArrayList    LATEST_EXCLUDED = new IntArrayList();

    private static GpuTexture     target;
    private static GpuTextureView targetView;
    private static boolean        captureDue;
    private static boolean        captureRequested;
    private static double         frameX;
    private static double         frameY;
    private static double         frameZ;
    private static double         capturedX;
    private static double         capturedY;
    private static double         capturedZ;
    private static int            framesSinceCapture = STILL_INTERVAL;
    private static long           lastCaptureNanos;
    private static long           generation;
    private static long           captures;
    private static long           latestSequence;
    private static long           latestVersion;
    private static double         latestX;
    private static double         latestY;
    private static double         latestZ;

    private OcclusionDepth() {
    }

    public static void describeFrame(float[] viewProjection, double cameraX, double cameraY, double cameraZ) {
        System.arraycopy(viewProjection, 0, FRAME_MATRIX, 0, FRAME_MATRIX.length);
        frameX = cameraX;
        frameY = cameraY;
        frameZ = cameraZ;
        framesSinceCapture ++;
        FRAME_ANIMATED.clear();
        captureDue = isCaptureDue();
    }

    public static void addFrameAnimated(int blockX, int blockY, int blockZ, int bounds) {
        FRAME_ANIMATED.add(blockX);
        FRAME_ANIMATED.add(blockY);
        FRAME_ANIMATED.add(blockZ);
        FRAME_ANIMATED.add(bounds);
    }

    public static boolean canCapture() {
        return captureDue;
    }

    public static void requestCapture() {
        captureRequested = true;
    }

    private static boolean isCaptureDue() {
        if (freeSlot() == null || System.nanoTime() - lastCaptureNanos < (captureRequested ? OcclusionCuller.MIN_INTERVAL : OcclusionCuller.captureInterval()))
            return false;

        boolean moved = frameX != capturedX || frameY != capturedY || frameZ != capturedZ || !Arrays.equals(FRAME_MATRIX, CAPTURED_MATRIX);

        return captureRequested || moved || framesSinceCapture >= STILL_INTERVAL;
    }

    public static void capture(RenderTarget mainTarget) {
        long start = System.nanoTime();
        Slot slot  = freeSlot();

        if (!captureDue || slot == null)
            throw new IllegalStateException("Depth capture requested without a described frame or a free slot");

        createTarget();

        //? if >= 26.3
        GpuTexture depth = mainTarget.getDepthTexture();

        //? if >= 26.3 {
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Polydium depth downsample", targetView, Optional.empty());
             MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer sizes = stack.malloc(PUSH_CONSTANTS);

            sizes.putInt(depth.getWidth(0)).putInt(depth.getHeight(0)).putInt(WIDTH).putInt(HEIGHT).flip();
            pass.setPipeline(RenderSystem.getCompiledPipeline(canGather() ? PIPELINE : FETCH_PIPELINE));
            pass.setUniform("DepthSampler", mainTarget.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.pushConstants(sizes);
            pass.draw(3, 1, 0, 0);
        }
        //?} else if >= 26.2 {
        /*try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Polydium depth downsample", targetView, Optional.empty())) {
            pass.setPipeline(PIPELINE);
            pass.bindTexture("DepthSampler", mainTarget.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(3, 1, 0, 0);
        }
        *///?} else {
        /*try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Polydium depth downsample", targetView, OptionalInt.empty())) {
            pass.setPipeline(PIPELINE);
            pass./^? if >= 1.21.11 {^/bindTexture("DepthSampler", mainTarget.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST))/^?} else {^//^bindSampler("DepthSampler", mainTarget.getDepthTextureView())^//^?}^/;
            /^? if < 1.21.10 {^//^pass.setVertexBuffer(0, RenderSystem.getQuadVertexBuffer());^//^?}^/
            pass.draw(0, 3);
        }
        *///?}

        long captureGeneration = generation;

        slot.pending  = true;
        slot.sequence = ++ captures;
        slot.x        = frameX;
        slot.y        = frameY;
        slot.z        = frameZ;
        System.arraycopy(FRAME_MATRIX, 0, slot.matrix, 0, FRAME_MATRIX.length);
        slot.excluded.clear();
        slot.excluded.addAll(FRAME_ANIMATED);
        System.arraycopy(FRAME_MATRIX, 0, CAPTURED_MATRIX, 0, FRAME_MATRIX.length);
        capturedX          = frameX;
        capturedY          = frameY;
        capturedZ          = frameZ;
        framesSinceCapture = 0;
        lastCaptureNanos   = System.nanoTime();
        captureDue         = false;
        captureRequested   = false;

        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(target, slot.buffer, /*? if >= 1.21.11 {*/0L/*?} else {*//*0*//*?}*/, () -> complete(slot, captureGeneration), 0);
        GpuWaitMeter.addCullCost(System.nanoTime() - start);
    }

    public static void reset() {
        generation ++;
        latestVersion      = 0L;
        captureDue         = false;
        captureRequested   = false;
        framesSinceCapture = STILL_INTERVAL;
    }

    public static void excludeFromPending(int blockX, int blockY, int blockZ, int bounds, long drawnThrough) {
        for (Slot slot : RING) {
            if (slot != null && slot.pending && slot.sequence <= drawnThrough) {
                slot.excluded.add(blockX);
                slot.excluded.add(blockY);
                slot.excluded.add(blockZ);
                slot.excluded.add(bounds);
            }
        }
    }

    public static long captureCount() {
        return captures;
    }

    public static long latestSequence() {
        return latestSequence;
    }

    public static long latestVersion() {
        return latestVersion;
    }

    public static float[] latestDepth() {
        return LATEST_DEPTH;
    }

    public static IntArrayList latestExcluded() {
        return LATEST_EXCLUDED;
    }

    public static float[] latestMatrix() {
        return LATEST_MATRIX;
    }

    public static double latestX() {
        return latestX;
    }

    public static double latestY() {
        return latestY;
    }

    public static double latestZ() {
        return latestZ;
    }

    //? if >= 26.3 {
    private static RenderPipeline.Builder pipelineBuilder() {
        return RenderPipeline.builder()
            .withBindGroupLayout(SAMPLER)
            .withPushConstantSize(PUSH_CONSTANTS)
            .withCull(false)
            .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth"))
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.R32_FLOAT, WRITE_RED))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES);
    }

    private static boolean canGather() {
        return !"OpenGL".equals(RenderSystem.getDevice().getDeviceInfo().backendName()) || GL.getCapabilities().GL_ARB_texture_gather;
    }
    //?}

    private static void complete(Slot slot, long captureGeneration) {
        slot.pending = false;

        if (captureGeneration != generation)
            return;

        long start = System.nanoTime();

        try (var view = /*? if >= 26.2 {*/slot.buffer.map(true, false)/*?} else {*//*RenderSystem.getDevice().createCommandEncoder().mapBuffer(slot.buffer, true, false)*//*?}*/) {
            view.data().order(ByteOrder.nativeOrder()).asFloatBuffer().get(0, LATEST_DEPTH);
        }

        System.arraycopy(slot.matrix, 0, LATEST_MATRIX, 0, LATEST_MATRIX.length);
        LATEST_EXCLUDED.clear();
        LATEST_EXCLUDED.addAll(slot.excluded);
        latestX = slot.x;
        latestY = slot.y;
        latestZ        = slot.z;
        latestSequence = slot.sequence;
        latestVersion ++;
        GpuWaitMeter.addCullCost(System.nanoTime() - start);
    }

    private static Slot freeSlot() {
        for (int index = 0; index < SLOTS; index ++) {
            if (RING[index] == null)
                RING[index] = new Slot();

            if (!RING[index].pending)
                return RING[index];
        }

        return null;
    }

    private static void createTarget() {
        if (target != null)
            return;

        target     = RenderSystem.getDevice().createTexture(() -> "Polydium occlusion depth", TEXTURE_USAGE, /*? if >= 26.2 {*/GpuFormat.R32_FLOAT/*?} else {*//*TextureFormat.RGBA8*//*?}*/, WIDTH, HEIGHT, 1, 1);
        targetView = RenderSystem.getDevice().createTextureView(target);
    }

    private static final class Slot {

        private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Polydium occlusion readback", BUFFER_USAGE, /*? if >= 1.21.11 {*/(long) TEXELS * Float.BYTES/*?} else {*//*TEXELS * Float.BYTES*//*?}*/);
        private final float[]   matrix = new float[16];
        private final IntArrayList excluded = new IntArrayList();
        private boolean         pending;
        private long            sequence;
        private double          x;
        private double          y;
        private double          z;
    }
}
//?}
