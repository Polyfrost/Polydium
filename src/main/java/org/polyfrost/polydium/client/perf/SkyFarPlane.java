//~ identifier
package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
//? if < 26.2
/*import com.mojang.blaze3d.shaders.UniformType;*/
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
//?}
import com.mojang.renderpearl.api.pipeline.BlendFunction;
//? if >= 26.1 {
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
//?}
//? if < 26.1
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
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
//? if < 26.2
/*import java.util.Optional;*/
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class SkyFarPlane {
    private static final String DEFINE = "POLYDIUM_FAR_PLANE";
    private static final String EXACT_DEFINE = "POLYDIUM_FAR_PLANE_EXACT";
    private static final String POSITION = "gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);";
    private static final String CIRRUS_POSITION = "gl_Position = clipPosition;";
    private static final Set<String> SHADERS = Set.of("minecraft:shaders/core/sky.vsh", "minecraft:shaders/core/stars.vsh", "minecraft:shaders/core/position_color.vsh", "minecraft:shaders/core/position_tex.vsh");
    private static final String NUIT_END_SKY_SHADER = "minecraft:shaders/core/position_tex_color.vsh";
    private static final String CIRRUS_MASK_SHADER = "cirrus:shaders/core/cirrus_cloud_mask.vsh";
    private static final Map<String, String> MOD_SHADERS = Map.of(
        "nuit:shaders/core/mono_color_skybox.vsh", POSITION,
        "nuit:shaders/core/textured_skybox.vsh", POSITION,
        "nuit:shaders/core/multi_textured_skybox.vsh", POSITION,
        "cirrus:shaders/core/cirrus_aurora.vsh", CIRRUS_POSITION,
        CIRRUS_MASK_SHADER, CIRRUS_POSITION,
        "cirrus:shaders/core/cirrus_lightning_sky.vsh", CIRRUS_POSITION,
        "cirrus:shaders/core/cirrus_milky_way.vsh", CIRRUS_POSITION,
        "cirrus:shaders/core/cirrus_stars.vsh", "gl_Position = ProjMat * ModelViewMat * vec4(expandedPosition, 1.0);"
    );
    private static final Set<String> PATCHED = ConcurrentHashMap.newKeySet();
    private static final boolean SKYBOXIFY = FabricLoader.getInstance().isModLoaded("skyboxify");
    private static final String SKYBOXIFY_SHADER = "skyboxify:shaders/core/custom_skybox.vsh";
    private static final boolean NUIT = FabricLoader.getInstance().isModLoaded("nuit");
    private static final boolean CIRRUS = FabricLoader.getInstance().isModLoaded("cirrus");
    private static final MethodHandle NUIT_ACTIVE_SKYBOXES = NUIT ? nuitActiveSkyboxes() : null;
    private static final boolean OVERFLOW = FabricLoader.getInstance().isModLoaded("overflowanimations");
    //? if >= 26.2 {
    private static final String[] OVERFLOW_UNHOOKED_SKY = {"planarSkyFog"};
    //?} else
    /*private static final String[] OVERFLOW_UNHOOKED_SKY = {"planarSkyFog", "blueVoidSky", "playerVoidBox"};*/
    private static final MethodHandle[] OVERFLOW_SKY_OPTIONS = OVERFLOW ? overflowSkyOptions() : null;
    private static final boolean SKY_MOD = Stream.of("optiboxes", "vitrail", "immersivevfx").anyMatch(FabricLoader.getInstance()::isModLoaded);
    private static final boolean VOXY_CLAMP = CIRRUS && !SKY_MOD && FabricLoader.getInstance().isModLoaded("voxy");
    private static final String VOXY_BLIT_SHADER = "voxy:post/blit_texture_depth_cutout.frag";
    private static final String VOXY_CLAMP_SOURCE = "CLOSER_SIGN*(2.0f/((1<<24)-1))";
    private static volatile boolean voxyClampPatched;
    //? if >= 26.3 {
    private static final String[] NUIT_HOOKS = {"polydium$farNuitBackendPipeline", "polydium$farNuitDiscPipeline", "polydium$hostNuitPass", "polydium$keepNuitHostOpen"};
    //?} else
    /*private static final String[] NUIT_HOOKS = {"polydium$farNuitBackendPipeline"};*/
    private static final String[] CIRRUS_HOOKS = {"polydium$farCirrusPipeline", "polydium$keepCirrusDepth"};

    static {
        // These classes load on their first draw, so their hooks could not be checked before deferring the sky
        if (NUIT) {
            preload("me.flashyreese.mods.nuit.render.NuitRenderBackend");
            //? if >= 26.3
            preload("me.flashyreese.mods.nuit.SkyboxManager$1");
        }
        if (CIRRUS) {
            preload("com.jvn.cirrus.client.render.CirrusVertexBuffer");
        }
    }

    private static final Map<RenderPipeline, RenderPipeline> FAR = new IdentityHashMap<>();
    private static final Map<RenderPipeline, RenderPipeline> FAR_DEPTH_WRITE = new IdentityHashMap<>();
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

        public static final RenderPipeline SUN = copy(CELESTIAL, "_sun", EXACT_DEFINE, false);
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

        public static final RenderPipeline SUN = copy(CELESTIAL, "_sun", EXACT_DEFINE, false);

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
    private static Operation<Void> deferredSky;
    private static SkyRenderer deferredRenderer;
    private static GpuBufferSlice deferredFog;
    private static SkyRenderState deferredState;
    private static RenderPass hostPass;
    //?} else {
    /*private static Runnable deferredSky;
    *///?}
    private static boolean drawing;
    private static boolean drawingSun;
    private static boolean blockDefer;

    private SkyFarPlane() {
    }

    private static void preload(String className) {
        try {
            Class.forName(className, false, SkyFarPlane.class.getClassLoader());
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static MethodHandle nuitActiveSkyboxes() {
        try {
            Class<?> manager = Class.forName("me.flashyreese.mods.nuit.SkyboxManager", false, SkyFarPlane.class.getClassLoader());
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return MethodHandles.filterReturnValue(
                lookup.findStatic(manager, "getInstance", MethodType.methodType(manager)),
                lookup.findVirtual(manager, "getActiveSkyboxes", MethodType.methodType(List.class)));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static MethodHandle[] overflowSkyOptions() {
        try {
            ClassLoader loader = SkyFarPlane.class.getClassLoader();
            Class<?> mod = Class.forName("org.polyfrost.overflowanimations.OverflowAnimations", false, loader);
            Class<?> config = Class.forName("org.polyfrost.overflowanimations.config.OverflowAnimationsConfig", false, loader);
            Class<?> other = Class.forName("org.polyfrost.overflowanimations.config.category.OtherConfigCategory", false, loader);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle otherCategory = MethodHandles.filterReturnValue(
                lookup.findStatic(config, "instance", MethodType.methodType(config)),
                lookup.findGetter(config, "other", other));
            MethodHandle[] handles = new MethodHandle[OVERFLOW_UNHOOKED_SKY.length + 1];
            handles[0] = lookup.findStatic(mod, "isEnabled", MethodType.methodType(boolean.class));
            for (int i = 0; i < OVERFLOW_UNHOOKED_SKY.length; i++) {
                handles[i + 1] = MethodHandles.filterReturnValue(otherCategory, lookup.findGetter(other, OVERFLOW_UNHOOKED_SKY[i], boolean.class));
            }
            return handles;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static boolean overflowSkyOverTerrain() {
        if (!OVERFLOW) {
            return false;
        }
        if (OVERFLOW_SKY_OPTIONS == null) {
            return true;
        }
        try {
            if (!(boolean) OVERFLOW_SKY_OPTIONS[0].invokeExact()) {
                return false;
            }
            for (int i = 1; i < OVERFLOW_SKY_OPTIONS.length; i++) {
                if ((boolean) OVERFLOW_SKY_OPTIONS[i].invokeExact()) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            return true;
        }
    }

    private static boolean nuitBuiltInSkyboxes() {
        if (NUIT_ACTIVE_SKYBOXES == null) {
            return false;
        }
        try {
            for (Object skybox : (List<?>) NUIT_ACTIVE_SKYBOXES.invoke()) {
                if (!skybox.getClass().getName().startsWith("me.flashyreese.mods.nuit.skybox.")) {
                    return false;
                }
            }
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static RenderPipeline far(RenderPipeline pipeline) {
        return FAR.computeIfAbsent(pipeline, source -> copy(source, "_polydium_far", DEFINE, false));
    }

    public static RenderPipeline farKeepingDepthWrite(RenderPipeline pipeline) {
        //? if >= 26.1 {
        boolean writesDepth = pipeline.getDepthStencilState() != null && pipeline.getDepthStencilState().writeDepth();
        //?} else
        /*boolean writesDepth = pipeline.isWriteDepth();*/
        return writesDepth ? FAR_DEPTH_WRITE.computeIfAbsent(pipeline, source -> copy(source, "_polydium_far_depth", DEFINE, true)) : far(pipeline);
    }

    //? if >= 26.2 {
    private static RenderPipeline copy(RenderPipeline source, String suffix, String define, boolean keepDepth) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
            .withLocation(source.getLocation().withSuffix(suffix))
            //? if >= 26.3 {
            .withVertexShader(source.getShaders().get(ShaderType.VERTEX))
            .withFragmentShader(source.getShaders().get(ShaderType.FRAGMENT))
            .withColorTargetState(source.getColorTargetStates().getFirst())
            //?} else {
            /*.withVertexShader(source.getVertexShader())
            .withFragmentShader(source.getFragmentShader())
            .withColorTargetState(source.getColorTargetState())
            *///?}
            .withShaderDefine(define)
            .withVertexBinding(0, source.getVertexFormatBinding(0))
            .withPrimitiveTopology(source.getPrimitiveTopology())
            .withPolygonMode(source.getPolygonMode())
            .withCull(source.isCull())
            .withDepthStencilState(keepDepth ? source.getDepthStencilState() : Pipelines.BEHIND_TERRAIN);
        source.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
        source.getShaderDefines().flags().forEach(builder::withShaderDefine);
        return builder.build();
    }
    //?} else {
    /*private static RenderPipeline copy(RenderPipeline source, String suffix, String define, boolean keepDepth) {
        return RenderPipeline.builder(new RenderPipeline.Snippet(
                Optional.of(source.getVertexShader()),
                Optional.of(source.getFragmentShader()),
                Optional.of(source.getShaderDefines()),
                Optional.of(source.getSamplers()),
                Optional.of(source.getUniforms()),
                /^? if >= 26.1 {^/
                Optional.of(source.getColorTargetState()),
                Optional.of(keepDepth ? source.getDepthStencilState() : new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)),
                Optional.of(source.getPolygonMode()),
                Optional.of(source.isCull()),
                /^?} else {^/
                /^source.getBlendFunction(),
                Optional.of(keepDepth ? source.getDepthTestFunction() : DepthTestFunction.LEQUAL_DEPTH_TEST),
                Optional.of(source.getPolygonMode()),
                Optional.of(source.isCull()),
                Optional.of(source.isWriteColor()),
                Optional.of(source.isWriteAlpha()),
                Optional.of(keepDepth && source.isWriteDepth()),
                Optional.of(source.getColorLogic()),
                ^//^?}^/
                Optional.of(source.getVertexFormat()),
                Optional.of(source.getVertexFormatMode())))
            .withLocation(source.getLocation().withSuffix(suffix))
            .withShaderDefine(define)
            .build();
    }
    *///?}

    private static String position(String id) {
        if (SHADERS.contains(id) || SKYBOXIFY_SHADER.equals(id) || NUIT_END_SKY_SHADER.equals(id)) {
            return POSITION;
        }
        //? if >= 26.2 {
        if (OVERFLOW_SKY_SHADER.equals(id)) {
            return OVERFLOW_SKY_POSITION;
        }
        //?}
        return MOD_SHADERS.get(id);
    }

    public static boolean isPatched(String id) {
        return PATCHED.contains(id);
    }

    private static boolean allPatched() {
        if (SKYBOXIFY && (!PolydiumMixinPlugin.skyboxifyFarSky() || !PATCHED.contains(SKYBOXIFY_SHADER))) {
            return false;
        }
        if (NUIT && (!PolydiumMixinPlugin.farSkyHooked(NUIT_HOOKS) || !modPatched("nuit:") || !PATCHED.contains(NUIT_END_SKY_SHADER) || !nuitBuiltInSkyboxes())) {
            return false;
        }
        if (CIRRUS && (!PolydiumMixinPlugin.farSkyHooked(CIRRUS_HOOKS) || !modPatched("cirrus:"))) {
            return false;
        }
        if (VOXY_CLAMP && !voxyClampPatched) {
            return false;
        }
        if (!DhLodDepth.ready()) {
            return false;
        }
        return PATCHED.containsAll(SHADERS);
    }

    public static String patchVoxy(String id, String source) {
        if (!VOXY_CLAMP || !VOXY_BLIT_SHADER.equals(id)) {
            return source;
        }
        voxyClampPatched = source.contains(VOXY_CLAMP_SOURCE);
        return source.replace(VOXY_CLAMP_SOURCE, "CLOSER_SIGN*4e-6");
    }

    private static boolean modPatched(String namespace) {
        for (String id : MOD_SHADERS.keySet()) {
            if (id.startsWith(namespace) && !PATCHED.contains(id)) {
                return false;
            }
        }
        return true;
    }

    public static String patch(Identifier location, String source) {
        String id = location.toString();
        String position = position(id);
        if (position == null) {
            return source;
        }
        if (!source.contains(position)) {
            PATCHED.remove(id);
            return source;
        }
        PATCHED.add(id);
        boolean mask = CIRRUS_MASK_SHADER.equals(id);
        //? if >= 26.3 {
        return source.replace(position, position
            + "\n#ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE" + depth(mask ? "5e-7 * gl_Position.w" : "1e-6 * gl_Position.w", "0.0")
            + "\n#else" + depth(mask ? "(1e-6 - 1.0) * gl_Position.w" : "(2e-6 - 1.0) * gl_Position.w", "-gl_Position.w")
            + "\n#endif");
        //?} else if >= 26.2 {
        /*boolean zeroToOne = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
        return source.replace(position, position + (zeroToOne
            ? depth(mask ? "5e-7 * gl_Position.w" : "1e-6 * gl_Position.w", "0.0")
            : depth(mask ? "(1e-6 - 1.0) * gl_Position.w" : "(2e-6 - 1.0) * gl_Position.w", "-gl_Position.w")));
        *///?} else {
        /*return source.replace(position, position + depth(mask ? "(1.0 - 1e-6) * gl_Position.w" : "(1.0 - 2e-6) * gl_Position.w", "gl_Position.w"));
        *///?}
    }

    private static String depth(String far, String exact) {
        return "\n#ifdef " + DEFINE + "\n    gl_Position.z = " + (CIRRUS ? far : exact) + ";\n#endif\n#ifdef " + EXACT_DEFINE + "\n    gl_Position.z = " + exact + ";\n#endif";
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
                && !overflowSkyOverTerrain()
                && allPatched();
    }

    //? if >= 26.3 {
    public static void defer(SkyRenderer renderer, GpuBufferSlice fog, SkyRenderState state, Operation<Void> sky) {
        deferredSky = sky;
        deferredRenderer = renderer;
        deferredFog = fog;
        deferredState = state;
        DhLodDepth.skyDeferred();
    }

    public static boolean hasDeferred() {
        return deferredState != null;
    }
    //?} else {
    /*public static void defer(Runnable sky) {
        deferredSky = sky;
        DhLodDepth.skyDeferred();
    }

    public static boolean hasDeferred() {
        return deferredSky != null;
    }
    *///?}

    //? if >= 26.3 {
    public static void drawDeferred(RenderPass host) {
    //?} else
    //public static void drawDeferred() {
        //? if >= 26.3 {
        Operation<Void> sky = deferredSky;
        SkyRenderer renderer = deferredRenderer;
        GpuBufferSlice fog = deferredFog;
        SkyRenderState state = deferredState;
        deferredSky = null;
        deferredRenderer = null;
        deferredFog = null;
        deferredState = null;
        //?} else {
        /*Runnable sky = deferredSky;
        deferredSky = null;
        *///?}
        //? if >= 26.3 {
        DhLodDepth.fill(host);
        //?} else
        /*DhLodDepth.fill();*/
        GpuBufferSlice mainFog = RenderSystem.getShaderFog();
        drawing = true;
        //? if >= 26.3
        hostPass = host;
        try {
            //? if >= 26.3 {
            sky.call(renderer, fog, state);
            //?} else {
            /*sky.run();
            *///?}
        } finally {
            drawing = false;
            drawingSun = false;
            //? if >= 26.3
            hostPass = null;
            RenderSystem.setShaderFog(mainFog);
        }
    }

    //? if >= 26.3 {
    public static RenderPass hostPass(GpuTextureView color, GpuTextureView depth) {
        RenderPass host = hostPass;
        if (host == null) {
            return null;
        }
        RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        if (color != main.getColorTextureView() || depth != main.getDepthTextureView()) {
            throw new IllegalStateException("The deferred sky draws into another target than the main pass");
        }
        return host;
    }

    public static boolean isHosting() {
        return hostPass != null;
    }
    //?}

    public static boolean isDrawing() {
        return drawing;
    }

    public static void drawingSun(boolean sun) {
        drawingSun = sun;
    }

    public static RenderPipeline celestial() {
        return drawingSun ? Pipelines.SUN : Pipelines.CELESTIAL;
    }
}
//?}
