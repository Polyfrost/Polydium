//~ identifier
package org.polyfrost.polydium.client.perf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
//? if >= 1.21.8
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? if >= 1.21.5
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4d;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.compat.Mods;
//? if >= 26.2 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
//? if >= 26.3
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.polyfrost.polydium.mixin.client.perf.OutlineScissorGroupAccessor;
import org.polyfrost.polydium.mixin.client.perf.OutlineScissorRendererAccessor;
import java.util.Map;
//?}

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public final class OutlineScissor {
    public static final int BOX_VALUES = 6;

    private static final int RASTER_REACH = 2;
    private static final int SOBEL_REACH = 2;
    private static final int BLUR_REACH = 4;
    private static final int MARGIN = RASTER_REACH + SOBEL_REACH + BLUR_REACH;
    private static final int PASSES = 4;
    private static final double MIN_CLIP_W = 0.01;
    private static final int UNMEASURED = 0;
    private static final int UNBOUNDED = 1;
    private static final int BOUNDED = 2;
    private static final boolean IRIS = Mods.loaded("iris");

    private static final String[] VANILLA_FILES = {
        "post_effect/entity_outline.json",
        "shaders/post/entity_outline.json",
        "shaders/post/entity_sobel.fsh",
        "shaders/post/entity_outline_box_blur.json",
        "shaders/post/entity_outline_box_blur.fsh",
        "shaders/post/blit.json",
        "shaders/post/blit.fsh",
        "shaders/post/blit.vsh",
        "shaders/post/sobel.vsh",
        "shaders/post/blur.vsh",
        "shaders/post/screenquad.vsh",
        "shaders/program/entity_outline.json",
        "shaders/program/entity_sobel.fsh",
        "shaders/program/entity_outline_box_blur.json",
        "shaders/program/entity_outline_box_blur.fsh",
        "shaders/program/blit.json",
        "shaders/program/blit.fsh",
        "shaders/program/blit.vsh",
        "shaders/program/sobel.vsh",
        "shaders/program/blur.vsh",
        "shaders/core/screenquad.vsh",
        "shaders/core/blit_screen.json",
        "shaders/core/blit_screen.vsh",
        "shaders/core/blit_screen.fsh",
        "shaders/core/rendertype_outline.json",
        "shaders/core/rendertype_outline.vsh",
        "shaders/include/dynamictransforms.glsl",
        "shaders/include/projection.glsl"
    };

    //? if >= 26.2 {
    private static final ClassValue<Boolean> PLAIN_EXECUTION = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                //? if >= 26.3 {
                return type.getMethod("executeGroup", FeatureFrameContext.class, OitStage.class, RenderPass.class, int.class, List.class, boolean.class).getDeclaringClass() == RenderTypeFeatureRenderer.class;
                //?} else {
                /*return type.getMethod("executeGroup", FeatureFrameContext.class, int.class, List.class, boolean.class).getDeclaringClass() == RenderTypeFeatureRenderer.class;
                *///?}
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    };
    //?} else {
    /*private static final float[] BOX = new float[BOX_VALUES];
    *///?}

    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final Matrix4d MODEL_VIEW = new Matrix4d();
    private static final Matrix4d CLIP = new Matrix4d();
    private static final Vector4d CORNER = new Vector4d();

    //? if >= 1.21.8
    private static GpuBufferSlice projectionSlice;
    private static PostChain chain;
    private static List<PostPass> passes = List.of();
    private static boolean vanilla;
    private static int state = UNMEASURED;
    private static boolean blitting;
    private static double minX;
    private static double minY;
    private static double maxX;
    private static double maxY;

    private OutlineScissor() {
    }

    public interface Passes {
        List<PostPass> polydium$passes();
    }

    //? if >= 26.2 {
    public interface DrawGroup {
        List<StagedVertexBuffer.Draw> polydium$draws();

        List<PreparedRenderType> polydium$drawRenderTypes();
    }

    public interface Draw {
        void polydium$trackOutline();

        float[] polydium$outlineBoxes();

        int polydium$outlineBoxValues();
    }

    public interface ModelView {
        Matrix4fc polydium$modelView();

        void polydium$setModelView(Matrix4fc modelView);
    }
    //?}

    public static boolean active() {
        return vanilla && PolydiumToggles.outlineScissor;
    }

    //? if >= 1.21.8 {
    public static void captureProjection(Matrix4fc projection, GpuBufferSlice slice) {
        PROJECTION.set(projection);
        projectionSlice = slice;
    }
    //?}

    public static void useChain(PostChain current) {
        if (current == chain)
            return;
        chain = current;
        if (current == null || IRIS) {
            passes = List.of();
            vanilla = false;
            return;
        }
        passes = ((Passes) current).polydium$passes();
        vanilla = passes.size() == PASSES && isVanillaChain();
    }

    //? if >= 26.2 {
    public static void beginFrame() {
        state = UNMEASURED;
    }

    public static void measure(SubmitNodeStorage storage, Map<FeatureRenderPhase<?>, ? extends List<?>> groupsByPhase, FeatureRendererMap renderers) {
        state = UNBOUNDED;
        if (!active() || storage == null || RenderSystem.getProjectionMatrixBuffer() != projectionSlice)
            return;

        resetBounds();
        for (SubmitNodeCollection collection : storage.getSubmitsPerOrder().values()) {
            List<?> groups = groupsByPhase.get(collection.outline);
            if (groups == null)
                continue;
            for (Object group : groups) {
                if (!includeGroup((OutlineScissorGroupAccessor) group, renderers))
                    return;
            }
        }
        state = BOUNDED;
    }
    //?} else {
    /*public static void beginFrame() {
        resetBounds();
        state = BOUNDED;
    }

    public static void measureDraw(boolean vanillaPipeline, MeshData mesh, Matrix4fc projection, Matrix4fc modelView) {
        if (state != BOUNDED || mesh.drawState().vertexCount() == 0)
            return;
        if (!vanillaPipeline || projection == null) {
            state = UNBOUNDED;
            return;
        }
        measureVertices(mesh, BOX, 0);
        CLIP.set(projection).mul(MODEL_VIEW.set(modelView));
        if (!includeBox(BOX, 0))
            state = UNBOUNDED;
    }
    *///?}

    //? if >= 1.21.8 && < 26.2 {
    /*public static Matrix4fc levelProjection() {
        return RenderSystem.getProjectionMatrixBuffer() == projectionSlice ? PROJECTION : null;
    }
    *///?}

    public static void measureVertices(MeshData mesh, float[] boxes, int offset) {
        MeshData.DrawState drawState = mesh.drawState();
        ByteBuffer buffer = mesh.vertexBuffer();
        int stride = drawState.format().getVertexSize();
        int count = drawState.vertexCount();
        if (drawState.format() != DefaultVertexFormat.POSITION_TEX_COLOR || buffer.remaining() != count * stride) {
            boxes[offset] = Float.NaN;
            return;
        }

        long address = MemoryUtil.memAddress(buffer);
        long end = address + (long) count * stride;
        float lowX = Float.POSITIVE_INFINITY, lowY = Float.POSITIVE_INFINITY, lowZ = Float.POSITIVE_INFINITY;
        float highX = Float.NEGATIVE_INFINITY, highY = Float.NEGATIVE_INFINITY, highZ = Float.NEGATIVE_INFINITY;
        float sum = 0.0F;
        for (long vertex = address; vertex < end; vertex += stride) {
            float x = MemoryUtil.memGetFloat(vertex);
            float y = MemoryUtil.memGetFloat(vertex + 4L);
            float z = MemoryUtil.memGetFloat(vertex + 8L);
            lowX = Math.min(lowX, x);
            lowY = Math.min(lowY, y);
            lowZ = Math.min(lowZ, z);
            highX = Math.max(highX, x);
            highY = Math.max(highY, y);
            highZ = Math.max(highZ, z);
            sum += x + y + z;
        }
        boxes[offset] = Float.isFinite(sum) ? lowX : Float.NaN;
        boxes[offset + 1] = lowY;
        boxes[offset + 2] = lowZ;
        boxes[offset + 3] = highX;
        boxes[offset + 4] = highY;
        boxes[offset + 5] = highZ;
    }

    public static boolean isOutlineChain(PostChain current) {
        return active() && current == chain;
    }

    public static boolean isOutlinePass(PostPass pass) {
        return active() && passes.contains(pass);
    }

    public static boolean isBlitting() {
        return blitting;
    }

    public static void beginBlit() {
        blitting = active();
    }

    public static void endBlit() {
        blitting = false;
        state = UNMEASURED;
    }

    public static boolean isEmpty(int width, int height) {
        if (state != BOUNDED)
            return false;
        return startX(width) >= endX(width) || startY(height) >= endY(height);
    }

    //? if >= 1.21.5 {
    public static void enableScissor(RenderPass renderPass, int width, int height) {
        if (state != BOUNDED)
            return;
        int startX = startX(width);
        int startY = startY(height);
        renderPass.enableScissor(startX, startY, endX(width) - startX, endY(height) - startY);
    }
    //?} else {
    /*public static boolean enableGlScissor(int width, int height) {
        if (state != BOUNDED)
            return false;
        if (isEmpty(width, height)) {
            RenderSystem.enableScissor(0, 0, 0, 0);
            return true;
        }
        int startX = startX(width);
        int startY = startY(height);
        RenderSystem.enableScissor(startX, startY, endX(width) - startX, endY(height) - startY);
        return true;
    }
    *///?}

    //? if < 1.21.4 {
    /*public static boolean dropsDepth(String chainName, String targetName) {
        return PolydiumToggles.outlineScissor && !IRIS && "minecraft:shaders/post/entity_outline.json".equals(chainName)
            && !"final".equals(targetName) && isVanillaChain();
    }
    *///?}

    private static void resetBounds() {
        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;
    }

    //? if >= 26.2 {
    private static boolean includeGroup(OutlineScissorGroupAccessor group, FeatureRendererMap renderers) {
        FeatureRenderer<?> renderer = renderers.getOrThrow(group.polydium$featureType());
        if (!(renderer instanceof RenderTypeFeatureRenderer<?> typed) || !PLAIN_EXECUTION.get(renderer.getClass()))
            return false;

        DrawGroup drawGroup = (DrawGroup) ((OutlineScissorRendererAccessor) typed).polydium$groups().get(group.polydium$featureGroupIndex());
        List<StagedVertexBuffer.Draw> draws = drawGroup.polydium$draws();
        List<PreparedRenderType> types = drawGroup.polydium$drawRenderTypes();
        for (int index = 0; index < draws.size(); index++) {
            if (!includeDraw(draws.get(index), types.get(index)))
                return false;
        }
        return true;
    }

    private static boolean includeDraw(StagedVertexBuffer.Draw draw, PreparedRenderType type) {
        if (draw.isEmpty())
            return true;
        RenderPipeline pipeline = type.pipeline();
        if (pipeline != RenderPipelines.OUTLINE_CULL && pipeline != RenderPipelines.OUTLINE_NO_CULL)
            return false;

        Draw outline = (Draw) draw;
        float[] boxes = outline.polydium$outlineBoxes();
        Matrix4fc modelView = ((ModelView) (Object) type).polydium$modelView();
        if (boxes == null || modelView == null)
            return false;

        CLIP.set(PROJECTION).mul(MODEL_VIEW.set(modelView));
        int values = outline.polydium$outlineBoxValues();
        for (int offset = 0; offset < values; offset += BOX_VALUES) {
            if (!includeBox(boxes, offset))
                return false;
        }
        return true;
    }
    //?}

    private static boolean includeBox(float[] boxes, int offset) {
        for (int value = offset; value < offset + BOX_VALUES; value++) {
            if (!Float.isFinite(boxes[value]))
                return false;
        }
        for (int corner = 0; corner < 8; corner++) {
            float x = boxes[offset + ((corner & 1) == 0 ? 0 : 3)];
            float y = boxes[offset + ((corner & 2) == 0 ? 1 : 4)];
            float z = boxes[offset + ((corner & 4) == 0 ? 2 : 5)];
            CLIP.transform(CORNER.set(x, y, z, 1.0));
            if (!(CORNER.w >= MIN_CLIP_W))
                return false;
            double ndcX = CORNER.x / CORNER.w;
            double ndcY = CORNER.y / CORNER.w;
            if (!Double.isFinite(ndcX) || !Double.isFinite(ndcY))
                return false;
            minX = Math.min(minX, ndcX);
            minY = Math.min(minY, ndcY);
            maxX = Math.max(maxX, ndcX);
            maxY = Math.max(maxY, ndcY);
        }
        return true;
    }

    private static int startX(int width) {
        return (int) Math.max(0.0, Math.floor((minX + 1.0) * 0.5 * width) - MARGIN);
    }

    private static int endX(int width) {
        return (int) Math.min(width, Math.ceil((maxX + 1.0) * 0.5 * width) + MARGIN);
    }

    private static int startY(int height) {
        return (int) Math.max(0.0, Math.floor((minY + 1.0) * 0.5 * height) - MARGIN);
    }

    private static int endY(int height) {
        return (int) Math.min(height, Math.ceil((maxY + 1.0) * 0.5 * height) + MARGIN);
    }

    private static boolean isVanillaChain() {
        for (String file : VANILLA_FILES) {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(Identifier.withDefaultNamespace(file));
            if (resource.isPresent() && !resource.get().sourcePackId().equals(BuiltInPackSource.VANILLA_ID))
                return false;
        }
        return true;
    }
}
