//~ identifier
package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
//? if < 26.2
/*import com.mojang.blaze3d.shaders.UniformType;*/
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
//? if >= 26.1 {
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
//?}
//? if >= 26.2
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? if < 26.2
/*import com.mojang.renderpearl.api.vertex.VertexFormat;*/
import net.fabricmc.loader.api.FabricLoader;
//? if >= 26.2
import net.minecraft.client.renderer.BindGroupLayouts;
//? if < 1.21.11
/*import net.minecraft.client.renderer.DimensionSpecialEffects;*/
import net.minecraft.client.renderer.SkyRenderer;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.SkyRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.SkyRenderState;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SkyFarPlane {
    private static final String DEFINE = "POLYDIUM_FAR_PLANE";
    private static final String POSITION = "gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);";
    private static final Set<String> SHADERS = Set.of("shaders/core/sky.vsh", "shaders/core/stars.vsh", "shaders/core/position_color.vsh", "shaders/core/position_tex.vsh");
    private static final Set<String> PATCHED = ConcurrentHashMap.newKeySet();
    private static final boolean SKY_MOD = java.util.stream.Stream.of("skyboxify", "optiboxes", "nuit").anyMatch(FabricLoader.getInstance()::isModLoaded);

    public static final class Pipelines {
        //? if >= 26.2 {
        private static final DepthStencilState BEHIND_TERRAIN = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);

        public static final RenderPipeline SKY = RenderPipeline.builder()
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            //? if >= 26.3 {
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            //?} else {
            /*.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            *///?}
            .withBindGroupLayout(BindGroupLayouts.FOG)
            .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/sky_far"))
            .withVertexShader("core/sky")
            .withFragmentShader("core/sky")
            .withShaderDefine(DEFINE)
            .withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(BEHIND_TERRAIN)
            .build();

        public static final RenderPipeline SUNRISE_SUNSET = RenderPipeline.builder()
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            //? if >= 26.3 {
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            //?} else {
            /*.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            *///?}
            .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/sunrise_sunset_far"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withShaderDefine(DEFINE)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
            .withDepthStencilState(BEHIND_TERRAIN)
            .build();

        public static final RenderPipeline STARS = RenderPipeline.builder()
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            //? if >= 26.3 {
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            //?} else {
            /*.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            *///?}
            .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/stars_far"))
            .withVertexShader("core/stars")
            .withFragmentShader("core/stars")
            .withShaderDefine(DEFINE)
            .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
            .withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(BEHIND_TERRAIN)
            .build();

        public static final RenderPipeline CELESTIAL = RenderPipeline.builder()
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            //? if >= 26.3 {
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            //?} else {
            /*.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            *///?}
            .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/celestial_far"))
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withShaderDefine(DEFINE)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(BEHIND_TERRAIN)
            .build();
        //?} else {
        /*public static final RenderPipeline SKY = far("sky_far", "sky", DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLE_FAN)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .build();

        public static final RenderPipeline SUNRISE_SUNSET = far("sunrise_sunset_far", "position_color", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
            /^? if >= 26.1 {^/.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))/^?} else {^//^.withBlend(BlendFunction.TRANSLUCENT)^//^?}^/
            .build();

        public static final RenderPipeline STARS = far("stars_far", "stars", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            /^? if >= 26.1 {^/.withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))/^?} else {^//^.withBlend(BlendFunction.OVERLAY)^//^?}^/
            .build();

        public static final RenderPipeline CELESTIAL = far("celestial_far", "position_tex", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            /^? if >= 26.1 {^/.withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))/^?} else {^//^.withBlend(BlendFunction.OVERLAY)^//^?}^/
            .build();

        private static RenderPipeline.Builder far(String name, String shader, VertexFormat format, VertexFormat.Mode mode) {
            return RenderPipeline.builder()
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/" + name))
                .withVertexShader("core/" + shader)
                .withFragmentShader("core/" + shader)
                .withShaderDefine(DEFINE)
                .withVertexFormat(format, mode)
                /^? if >= 26.1 {^/.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))/^?} else {^//^.withDepthWrite(false)^//^?}^/;
        }
        *///?}

        private Pipelines() {
        }
    }

    //? if >= 26.3 {
    private static SkyRenderer deferredRenderer;
    private static GpuBufferSlice deferredFog;
    private static SkyRenderState deferredState;
    //?} else {
    /*private static Runnable deferredSky;
    *///?}
    private static boolean drawing;

    private SkyFarPlane() {
    }

    public static String patch(Identifier location, String source) {
        if (!location.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) || !SHADERS.contains(location.getPath())) {
            return source;
        }
        if (!source.contains(POSITION)) {
            PATCHED.remove(location.getPath());
            return source;
        }
        PATCHED.add(location.getPath());
        //? if >= 26.3 {
        return source.replace(POSITION, POSITION + "\n#ifdef " + DEFINE
            + "\n#ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE\n    gl_Position.z = 0.0;\n#else\n    gl_Position.z = -gl_Position.w;\n#endif\n#endif");
        //?} else if >= 26.2 {
        /*String far = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne() ? "0.0" : "-gl_Position.w";
        return source.replace(POSITION, POSITION + "\n#ifdef " + DEFINE + "\n    gl_Position.z = " + far + ";\n#endif");
        *///?} else {
        /*return source.replace(POSITION, POSITION + "\n#ifdef " + DEFINE + "\n    gl_Position.z = gl_Position.w;\n#endif");
        *///?}
    }

    public static boolean canDefer(SkyRenderState state, boolean improvedTransparency) {
        //? if >= 1.21.11 {
        return state.skybox == DimensionType.Skybox.OVERWORLD
                && !IrisShaders.packInUse()
        //?} else {
        /*return state.skyType == DimensionSpecialEffects.SkyType.OVERWORLD
                && !IrisShaders.packInUse()
        *///?}
                && !improvedTransparency
                && !SKY_MOD
                && PATCHED.size() == SHADERS.size();
    }

    //? if >= 26.3 {
    public static void defer(SkyRenderer renderer, GpuBufferSlice fog, SkyRenderState state) {
        deferredRenderer = renderer;
        deferredFog = fog;
        deferredState = state;
    }

    public static boolean hasDeferred() {
        return deferredState != null;
    }
    //?} else {
    /*public static void defer(Runnable sky) {
        deferredSky = sky;
    }

    public static boolean hasDeferred() {
        return deferredSky != null;
    }
    *///?}

    public static void drawDeferred() {
        //? if >= 26.3 {
        SkyRenderer renderer = deferredRenderer;
        GpuBufferSlice fog = deferredFog;
        SkyRenderState state = deferredState;
        deferredRenderer = null;
        deferredFog = null;
        deferredState = null;
        //?} else {
        /*Runnable sky = deferredSky;
        deferredSky = null;
        *///?}
        GpuBufferSlice mainFog = RenderSystem.getShaderFog();
        drawing = true;
        try {
            //? if >= 26.3 {
            renderer.render(fog, state);
            //?} else {
            /*sky.run();
            *///?}
        } finally {
            drawing = false;
            RenderSystem.setShaderFog(mainFog);
        }
    }

    public static boolean isDrawing() {
        return drawing;
    }
}
//?}
