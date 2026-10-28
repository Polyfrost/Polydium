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
//? if = 1.21.11
/*import com.mojang.blaze3d.platform.DepthTestFunction;*/
//? if >= 26.2
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? if >= 26.3
import com.mojang.renderpearl.api.pipeline.ShaderType;
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
//? if >= 1.21.11
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;

//? if >= 1.21.11 {
import java.util.IdentityHashMap;
import java.util.Map;
//?}
//? if >= 1.21.11 && < 26.2
/*import java.util.Optional;*/
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class SkyFarPlane {
    private static final String DEFINE = "POLYDIUM_FAR_PLANE";
    private static final String POSITION = "gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);";
    private static final Set<String> SHADERS = Set.of("minecraft:shaders/core/sky.vsh", "minecraft:shaders/core/stars.vsh", "minecraft:shaders/core/position_color.vsh", "minecraft:shaders/core/position_tex.vsh");
    private static final Set<String> PATCHED = ConcurrentHashMap.newKeySet();
    //? if >= 1.21.11 {
    private static final boolean SKY_MOD = Stream.of("optiboxes", "nuit").anyMatch(FabricLoader.getInstance()::isModLoaded);
    private static final boolean SKYBOXIFY = FabricLoader.getInstance().isModLoaded("skyboxify");
    private static final String SKYBOXIFY_SHADER = "skyboxify:shaders/core/custom_skybox.vsh";
    private static final Map<RenderPipeline, RenderPipeline> FAR = new IdentityHashMap<>();
    //?} else {
    /*private static final boolean SKY_MOD = Stream.of("skyboxify", "optiboxes", "nuit").anyMatch(FabricLoader.getInstance()::isModLoaded);
    *///?}
    //? if >= 26.2 {
    public static final String OVERFLOW_SKY_SHADER = "overflowanimations:shaders/core/legacy_sky.vsh";
    private static final String OVERFLOW_SKY_POSITION = "gl_Position = ProjMat * eye;";
    //?}

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
    private static boolean blockDefer;

    private SkyFarPlane() {
    }

    //? if >= 26.2 {
    public static RenderPipeline far(RenderPipeline pipeline) {
        return FAR.computeIfAbsent(pipeline, source -> {
            RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(source.getLocation().withSuffix("_polydium_far"))
                //? if >= 26.3 {
                .withVertexShader(source.getShaders().get(ShaderType.VERTEX))
                .withFragmentShader(source.getShaders().get(ShaderType.FRAGMENT))
                .withColorTargetState(source.getColorTargetStates().getFirst())
                //?} else {
                /*.withVertexShader(source.getVertexShader())
                .withFragmentShader(source.getFragmentShader())
                .withColorTargetState(source.getColorTargetState())
                *///?}
                .withShaderDefine(DEFINE)
                .withVertexBinding(0, source.getVertexFormatBinding(0))
                .withPrimitiveTopology(source.getPrimitiveTopology())
                .withDepthStencilState(Pipelines.BEHIND_TERRAIN);
            source.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
            source.getShaderDefines().flags().forEach(builder::withShaderDefine);
            return builder.build();
        });
    }
    //?} else if >= 1.21.11 {
    /*public static RenderPipeline far(RenderPipeline pipeline) {
        return FAR.computeIfAbsent(pipeline, source -> RenderPipeline.builder(new RenderPipeline.Snippet(
                Optional.of(source.getVertexShader()),
                Optional.of(source.getFragmentShader()),
                Optional.of(source.getShaderDefines()),
                Optional.of(source.getSamplers()),
                Optional.of(source.getUniforms()),
                /^? if >= 26.1 {^/
                Optional.of(source.getColorTargetState()),
                Optional.of(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)),
                Optional.of(source.getPolygonMode()),
                Optional.of(source.isCull()),
                /^?} else {^/
                /^source.getBlendFunction(),
                Optional.of(DepthTestFunction.LEQUAL_DEPTH_TEST),
                Optional.of(source.getPolygonMode()),
                Optional.of(source.isCull()),
                Optional.of(source.isWriteColor()),
                Optional.of(source.isWriteAlpha()),
                Optional.of(false),
                Optional.of(source.getColorLogic()),
                ^//^?}^/
                Optional.of(source.getVertexFormat()),
                Optional.of(source.getVertexFormatMode())))
            .withLocation(source.getLocation().withSuffix("_polydium_far"))
            .withShaderDefine(DEFINE)
            .build());
    }
    *///?}

    private static boolean isFarShader(String id) {
        //? if >= 26.2 {
        return SHADERS.contains(id) || SKYBOXIFY_SHADER.equals(id) || OVERFLOW_SKY_SHADER.equals(id);
        //?} else if >= 1.21.11 {
        /*return SHADERS.contains(id) || SKYBOXIFY_SHADER.equals(id);
        *///?} else
        /*return SHADERS.contains(id);*/
    }

    public static boolean isPatched(String id) {
        return PATCHED.contains(id);
    }

    private static boolean allPatched() {
        //? if >= 1.21.11 {
        if (SKYBOXIFY && (!PolydiumMixinPlugin.skyboxifyFarSky() || !PATCHED.contains(SKYBOXIFY_SHADER))) {
            return false;
        }
        //?}
        return PATCHED.containsAll(SHADERS);
    }

    public static String patch(Identifier location, String source) {
        String id = location.toString();
        if (!isFarShader(id)) {
            return source;
        }
        //? if >= 26.2 {
        String position = OVERFLOW_SKY_SHADER.equals(id) ? OVERFLOW_SKY_POSITION : POSITION;
        //?} else
        /*String position = POSITION;*/
        if (!source.contains(position)) {
            PATCHED.remove(id);
            return source;
        }
        PATCHED.add(id);
        //? if >= 26.3 {
        return source.replace(position, position + "\n#ifdef " + DEFINE
            + "\n#ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE\n    gl_Position.z = 0.0;\n#else\n    gl_Position.z = -gl_Position.w;\n#endif\n#endif");
        //?} else if >= 26.2 {
        /*String far = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne() ? "0.0" : "-gl_Position.w";
        return source.replace(position, position + "\n#ifdef " + DEFINE + "\n    gl_Position.z = " + far + ";\n#endif");
        *///?} else {
        /*return source.replace(position, position + "\n#ifdef " + DEFINE + "\n    gl_Position.z = gl_Position.w;\n#endif");
        *///?}
    }

    public static void blockNextDefer() {
        blockDefer = true;
    }

    public static boolean canDefer(SkyRenderState state, boolean improvedTransparency) {
        boolean blocked = blockDefer;
        blockDefer = false;
        //? if >= 1.21.11 {
        return !blocked
                && state.skybox == DimensionType.Skybox.OVERWORLD
                && !IrisShaders.packInUse()
        //?} else {
        /*return !blocked
                && state.skyType == DimensionSpecialEffects.SkyType.OVERWORLD
                && !IrisShaders.packInUse()
        *///?}
                && !improvedTransparency
                && !SKY_MOD
                && allPatched();
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
