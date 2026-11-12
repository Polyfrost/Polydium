//~ identifier
package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
//? if >= 1.21.11 {
import com.mojang.renderpearl.api.textures.FilterMode;
//?} else {
/*import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
*///?}
//? if >= 26.1 {
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
//?} else
/*import com.mojang.blaze3d.platform.DepthTestFunction;*/
//? if >= 26.2 {
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
//?} else {
/*import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.OptionalInt;
*///?}
//? if >= 26.3 {
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.backend.opengl.GlTexture;
//?} else
/*import com.mojang.blaze3d.opengl.GlTexture;*/
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
//? if >= 1.21.11 && < 26.2
/*import org.polyfrost.polydium.vulkan.VulkanActive;*/

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.OptionalDouble;

public final class DhLodDepth {
    public static final boolean LOADED = FabricLoader.getInstance().isModLoaded("distanthorizons");
    private static final String BLAZE_HOOK = "polydium$dhBlazeLodsApplied";
    private static final String GL_HOOK = "polydium$dhGlLodsApplied";
    private static final String GL_FADE_HOOK = "polydium$dhGlFadeSource";
    private static final String SAMPLER = "DhDepthSampler";
    private static final String REVERSED_GROUND = "(mcFragmentDepth > 0)";
    private static final String GROUND = "(mcFragmentDepth < 1.0)";

    //? if >= 26.3 {
    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/dh_lod_depth"))
        .withBindGroupLayout(BindGroupLayout.builder().withUniform(SAMPLER, UniformType.COMBINED_IMAGE_SAMPLER).build())
        .withCull(false)
        .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/dh_lod_depth"))
        .withShaderDefine("REVERSED_DEPTH")
        .withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.DEFAULT.format(), ColorTargetState.WRITE_NONE))
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
    //?} else if >= 26.2 {
    /*private static final RenderPipeline PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/dh_lod_depth"))
        .withBindGroupLayout(BindGroupLayout.builder().withSampler(SAMPLER).build())
        .withCull(false)
        .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth_gl"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/dh_lod_depth_gl"))
        .withShaderDefine("REVERSED_DEPTH")
        .withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.DEFAULT.format(), ColorTargetState.WRITE_NONE))
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
    *///?} else {
    /*private static final RenderPipeline PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/dh_lod_depth"))
        .withSampler(SAMPLER)
        .withCull(false)
        .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/occlusion_depth_gl"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/dh_lod_depth_gl"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        /^? if >= 26.1 {^/
        .withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.WRITE_NONE))
        .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
        /^?} else {^/
        /^.withColorWrite(false)
        .withDepthWrite(true)
        .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
        ^//^?}^/
        .build();
    *///?}

    private static final MethodHandle RENDER_PROXY;
    private static final MethodHandle ENGINE;
    private static final MethodHandle BLAZE_DEPTH;
    private static final MethodHandle GL_DEPTH;
    private static final MethodHandle SUCCESS;
    private static final MethodHandle PAYLOAD;
    private static final MethodHandle WRAPPED;

    static {
        MethodHandle[] handles = LOADED ? resolve() : null;
        RENDER_PROXY = handles == null ? null : handles[0];
        ENGINE = handles == null ? null : handles[1];
        BLAZE_DEPTH = handles == null ? null : handles[2];
        GL_DEPTH = handles == null ? null : handles[3];
        SUCCESS = handles == null ? null : handles[4];
        PAYLOAD = handles == null ? null : handles[5];
        WRAPPED = handles == null ? null : handles[6];
    }

    private static boolean applied;
    private static volatile boolean fadeUnpatched;
    private static int glId = -1;
    private static GpuTexture glTexture;
    private static GpuTextureView glTextureView;

    private DhLodDepth() {
    }

    private static MethodHandle[] resolve() {
        try {
            ClassLoader loader = DhLodDepth.class.getClassLoader();
            Class<?> delayed = Class.forName("com.seibel.distanthorizons.api.DhApi$Delayed", false, loader);
            Class<?> proxy = Class.forName("com.seibel.distanthorizons.api.interfaces.render.IDhApiRenderProxy", false, loader);
            Class<?> result = Class.forName("com.seibel.distanthorizons.api.objects.DhApiResult", false, loader);
            Class<?> unsafe = Class.forName("com.seibel.distanthorizons.api.interfaces.IDhApiUnsafeWrapper", false, loader);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return new MethodHandle[]{
                lookup.findStaticGetter(delayed, "renderProxy", proxy),
                lookup.unreflect(proxy.getMethod("getRenderingEngine")),
                lookup.unreflect(proxy.getMethod("getDhDepthTextureBlazeWrapper")),
                lookup.unreflect(proxy.getMethod("getDhDepthTextureGlId")),
                lookup.findGetter(result, "success", boolean.class),
                lookup.findGetter(result, "payload", Object.class),
                lookup.findVirtual(unsafe, "getWrappedMcObject", MethodType.methodType(Object.class))
            };
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    public static boolean ready() {
        if (!LOADED) {
            return true;
        }
        if (RENDER_PROXY == null || fadeUnpatched) {
            return false;
        }
        String engine = engine();
        if ("BLAZE_3D".equals(engine)) {
            return PolydiumMixinPlugin.farSkyHooked(BLAZE_HOOK);
        }
        //? if >= 26.2 {
        boolean gl = "OpenGL".equals(RenderSystem.getDevice().getDeviceInfo().backendName());
        //?} else if >= 1.21.11 {
        /*boolean gl = !VulkanActive.active;
        *///?} else
        /*boolean gl = true;*/
        return "OPEN_GL".equals(engine) && gl && PolydiumMixinPlugin.farSkyHooked(GL_HOOK, GL_FADE_HOOK);
    }

    public static String patchFade(String id, String source) {
        if (!LOADED || !id.endsWith("fade/blaze/vanilla_fade.fsh") && !id.endsWith("fade/gl/vanilla_fade.frag")) {
            return source;
        }
        if (!source.contains(REVERSED_GROUND) || !source.contains(GROUND)) {
            fadeUnpatched = true;
            return source;
        }
        return source.replace(REVERSED_GROUND, "(mcFragmentDepth > 5e-6)").replace(GROUND, "(mcFragmentDepth < 1.0 - 5e-6)");
    }

    private static String engine() {
        try {
            Object proxy = RENDER_PROXY.invoke();
            return proxy == null ? null : ((Enum<?>) ENGINE.invoke(proxy)).name();
        } catch (Throwable e) {
            return null;
        }
    }

    public static void skyDeferred() {
        applied = false;
    }

    public static void lodsApplied() {
        applied = true;
    }

    //? if >= 26.3 {
    public static void fill(RenderPass host) {
    //?} else
    //public static void fill() {
        if (!applied) {
            return;
        }
        applied = false;
        RenderTarget main = /*? if >= 26.2 {*/Minecraft.getInstance().gameRenderer.mainRenderTarget()/*?} else {*//*Minecraft.getInstance().getMainRenderTarget()*//*?}*/;
        GpuTextureView depth = depthView(main);
        if (depth == null) {
            return;
        }
        //? if >= 26.3 {
        if (host != null) {
            draw(host, depth);
            return;
        }
        //?}
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Polydium DH LOD depth", main.getColorTextureView(), /*? if >= 26.2 {*/Optional.empty()/*?} else {*//*OptionalInt.empty()*//*?}*/, main.getDepthTextureView(), OptionalDouble.empty())) {
            draw(pass, depth);
        }
    }

    private static void draw(RenderPass pass, GpuTextureView depth) {
        //? if >= 26.3 {
        pass.setPipeline(RenderSystem.getCompiledPipeline(PIPELINE));
        pass.setUniform(SAMPLER, depth, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
        pass.draw(3, 1, 0, 0);
        //?} else if >= 26.2 {
        /*pass.setPipeline(PIPELINE);
        pass.bindTexture(SAMPLER, depth, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
        pass.draw(3, 1, 0, 0);
        *///?} else {
        /*pass.setPipeline(PIPELINE);
        pass./^? if >= 1.21.11 {^/bindTexture(SAMPLER, depth, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST))/^?} else {^//^bindSampler(SAMPLER, depth)^//^?}^/;
        pass.draw(0, 3);
        *///?}
    }

    private static GpuTextureView depthView(RenderTarget main) {
        try {
            Object proxy = RENDER_PROXY.invoke();
            if (proxy == null) {
                return null;
            }
            String engine = ((Enum<?>) ENGINE.invoke(proxy)).name();
            if ("BLAZE_3D".equals(engine)) {
                Object result = BLAZE_DEPTH.invoke(proxy);
                if (!(boolean) SUCCESS.invoke(result)) {
                    return null;
                }
                return ((Object[]) WRAPPED.invoke(PAYLOAD.invoke(result)))[1] instanceof GpuTextureView view ? view : null;
            }
            if ("OPEN_GL".equals(engine)) {
                Object result = GL_DEPTH.invoke(proxy);
                return (boolean) SUCCESS.invoke(result) ? glView((Integer) PAYLOAD.invoke(result), main) : null;
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    private static GpuTextureView glView(int id, RenderTarget main) {
        int width = main.width;
        int height = main.height;
        if (id != glId || glTexture.getWidth(0) != width || glTexture.getHeight(0) != height) {
            if (glTextureView != null) {
                glTextureView.close();
            }
            //? if >= 26.2 {
            glTexture = new GlTexture(GpuTexture.USAGE_TEXTURE_BINDING, "Distant Horizons depth", GpuFormat.D32_FLOAT, width, height, 1, 1, id, null) {
            //?} else
            /*glTexture = new GlTexture(GpuTexture.USAGE_TEXTURE_BINDING, "Distant Horizons depth", TextureFormat.DEPTH32, width, height, 1, 1, id) {*/
                @Override
                public void close() {
                }
            };
            //? if < 1.21.11 {
            /*// Binding writes these onto the GL texture itself, so they must match the NEAREST / clamp DH created it with
            glTexture.setTextureFilter(FilterMode.NEAREST, false);
            glTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
            *///?}
            glTextureView = RenderSystem.getDevice().createTextureView(glTexture);
            glId = id;
        }
        return glTextureView;
    }
}
//?}
