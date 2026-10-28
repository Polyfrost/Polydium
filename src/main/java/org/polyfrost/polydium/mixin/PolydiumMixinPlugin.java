package org.polyfrost.polydium.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import org.polyfrost.polydium.compat.Mods;
import org.polyfrost.polydium.early.EarlyVulkan;
//? if >= 1.21.11 && < 26.2
/*import org.polyfrost.polydium.vulkan.IrisVulkanGate;*/
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolydiumMixinPlugin implements IMixinConfigPlugin {
    //? if >= 26.1 {
    private static final String GL_TEXTURE = "com/mojang/blaze3d/opengl/GlTexture", GL_ID = "glId", RENDER_TARGET = "Lcom/mojang/blaze3d/pipeline/RenderTarget;";
    //?} else
    /*private static final String GL_TEXTURE = "net/minecraft/class_10868", GL_ID = "method_68427", RENDER_TARGET = "Lnet/minecraft/class_276;";*/
    private static final String SODIUM_PERF_PACKAGE = "org.polyfrost.polydium.mixin.client.perf.sodium.";
    //? if >= 26.3 {
    private static final String[] SODIUM_PERF_VERSIONS = {"0.9.2", "0.9.3"};
    //?} else if >= 26.2 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.9.2"};
    *///?} else if >= 26.1 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.9.2", "0.8.9", "0.8.10", "0.8.11", "0.8.12"};
    *///?} else if >= 1.21.11 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.8.14", "0.8.15"};
    *///?} else if >= 1.21.8 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.7.3"};
    *///?} else if >= 1.21.4 {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.6.13"};
    *///?} else {
    /*private static final String[] SODIUM_PERF_VERSIONS = {"0.8.13"};
    *///?}

    private static final String SODIUM_VERSION = FabricLoader.getInstance().getModContainer("sodium")
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
            .orElse("");

    public static final boolean SODIUM_PERF = Arrays.stream(SODIUM_PERF_VERSIONS).anyMatch(SODIUM_VERSION::startsWith);

    private static volatile ClassNode skyboxifyRenderer;
    private static boolean skyboxifyFarSky;

    private static final String SODIUM_08_PACKAGE = SODIUM_PERF_PACKAGE + "v08.";
    private static final Set<String> SODIUM_09_ONLY = Set.of(
            SODIUM_PERF_PACKAGE + "MixinAnimatedSpriteMarksRegion",
            SODIUM_PERF_PACKAGE + "MixinSpriteSetRegion"
    );
    //? if >= 26.1 && < 26.2 {
    /*private static final boolean SODIUM_08 = SODIUM_VERSION.startsWith("0.8.");
    *///?} else {
    private static final boolean SODIUM_08 = false;
    //?}
    public static final boolean SODIUM_V08 = SODIUM_VERSION.startsWith("0.8.");
    private static final String SODIUM_VULKAN_PACKAGE = SODIUM_PERF_PACKAGE + "vulkan.";
    private static final String[] SODIUM_VULKAN_09_ONLY = {"MixinVulkanGlTexelBuffer", "MixinVulkanGpuBufferHandle", "MixinVulkanChunkRenderer"};

    private static final String[] DISABLED = System.getProperty("polydium.disable", "").split(",");

    private static final String[] ENABLED = System.getProperty("polydium.enable", "").split(",");

    private static final Map<String, String[]> STAND_DOWN = Map.ofEntries(
            Map.entry("TextWidthCache", new String[]{"modernui"}),
            Map.entry("LtrTextFastPath", new String[]{"modernui"}),
            Map.entry("PreparedText", new String[]{"caxton"}),
            Map.entry("PreparedLanguage", new String[]{"languagereload"}),
            Map.entry("ParticleRotationReuse", new String[]{"vulkanmod"}),
            Map.entry("SinglePassTextShadow", new String[]{"caxton"}),
            Map.entry("VulkanBackendList", new String[]{"vulkanmod", "modernui", "longview", "photonics", "physicsmod"}),
            Map.entry("GraphicsApiOption", new String[]{"vulkanmod", "modernui", "longview", "photonics", "physicsmod"})
    );

    private static final String[] OPT_IN = {"UnfocusedFullscreenThrottle"};

    private static boolean vulkanBackend() {
        return !isDisabled("VulkanBackendList") && (contains(ENABLED, "VulkanBackendList") || EarlyVulkan.vulkanAtStartup());
    }

    public static boolean isEnabled(String fragment) {
        return contains(ENABLED, fragment) && !isDisabled(fragment);
    }

    public static boolean skyboxifyFarSky() {
        ClassNode target = skyboxifyRenderer;
        if (target != null) {
            skyboxifyRenderer = null;
            Set<String> handlers = new HashSet<>(), called = new HashSet<>();
            for (MethodNode method : target.methods) {
                if (method.name.contains("polydium$"))
                    handlers.add(method.name);
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof MethodInsnNode call && call.owner.equals(target.name))
                        called.add(call.name);
            }
            skyboxifyFarSky = !handlers.isEmpty() && called.containsAll(handlers);
        }
        return skyboxifyFarSky;
    }

    private static boolean contains(String[] fragments, String name) {
        for (String fragment : fragments) {
            if (!fragment.isEmpty() && (name.contains(fragment) || fragment.contains(name)))
                return true;
        }

        return false;
    }

    private static boolean standsDown(String mixinClassName) {
        if ((mixinClassName.contains("VulkanBackendList") || mixinClassName.contains("GraphicsApiOption")) && FabricLoader.getInstance().getModContainer("distanthorizons")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString().startsWith("2.")).orElse(false))
            return true;

        if (mixinClassName.contains("PresentThread") && Mods.loaded("slipstream") && System.getProperty("os.name", "").startsWith("Mac"))
            return true;

        for (Map.Entry<String, String[]> entry : STAND_DOWN.entrySet()) {
            if (!mixinClassName.contains(entry.getKey()))
                continue;

            for (String mod : entry.getValue()) {
                if (Mods.loaded(mod)) {
                    LoggerFactory.getLogger("polydium/perf").debug("{} stands down: {} is loaded", mixinClassName, mod);
                    return true;
                }
            }
        }

        return false;
    }

    private static final String[] GL_BUFFER_UNIT = {
            "org.polyfrost.polydium.mixin.client.perf.opengl.MixinGlBufferMapFlags",
            "org.polyfrost.polydium.mixin.client.perf.opengl.MixinGlBufferNoUnmapFlush",
            "org.polyfrost.polydium.mixin.client.perf.opengl.MixinGlBufferStorageCoherent"
    };

    public static boolean isDisabled(String mixinClassName) {
        if (contains(DISABLED, mixinClassName))
            return true;

        if (mixinClassName.contains(".backport.") && mixinClassName.endsWith("Backport")) {
            String vanilla = mixinClassName.replace(".backport.", ".");
            if (contains(DISABLED, vanilla.substring(0, vanilla.length() - "Backport".length())))
                return true;
        }

        for (String member : GL_BUFFER_UNIT) {
            if (!member.equals(mixinClassName))
                continue;

            for (String other : GL_BUFFER_UNIT) {
                if (contains(DISABLED, other))
                    return true;
            }
        }

        return false;
    }

    private static boolean xaeroCompatible(String targetClassName, String mixinClassName) {
        if (!Mods.loaded("xaerominimap") && !Mods.loaded("xaerominimapfair")) return false;
        boolean compatible;
        try {
            compatible = xaeroMatches(xaeroNode(targetClassName));
        } catch (Exception e) {
            compatible = false;
        }
        if (!compatible) LoggerFactory.getLogger("polydium/perf").warn("Installed Xaero's Minimap doesn't match {}, skipping it", mixinClassName);
        return compatible;
    }

    private static boolean xaeroMatches(ClassNode target) throws Exception {
        //? if < 1.21.6 {
        /*MethodNode render = xaeroMethod(target, "renderChunksToFBO");
        return render != null && target.fields.stream().anyMatch(f -> f.name.equals("rotationFramebuffer"))
            && xaeroArgs(render, Type.INT_TYPE) >= 3 && xaeroArgs(render, Type.BOOLEAN_TYPE) >= 3
            && xaeroMethod(xaeroNode("xaero.hud.compat.mods.ImmediatelyFastHelper"), "triggerBatchingBuffersFlush") != null;
        *///?} else {
        ClassNode state = xaeroNode("xaero.hud.minimap.render.MinimapPipRenderState");
        for (String getter : List.of("getWidth", "getHeight", "getScale", "getMinimapScale", "getSize", "getBoxSize")) {
            if (xaeroMethod(state, getter) == null) return false;
        }
        if (xaeroMethod(xaeroNode("xaero.common.minimap.MinimapProcessor"), "updateZoom") == null) return false;
        if (xaeroMethod(target, "textureIsReadyToBlit") != null) return false;
        //? if >= 26.3 {
        MethodNode prepare = xaeroMethod(target, "prepare");
        if (prepare == null || !xaeroInvokes(prepare, "xaero_mm_prepareTexturesAndProjection") || !xaeroInvokes(prepare, "renderToTexture")) return false;
        //?}
        return true;
        //?}
    }

    private static ClassNode xaeroNode(String name) throws Exception {
        return MixinService.getService().getBytecodeProvider().getClassNode(name);
    }

    private static MethodNode xaeroMethod(ClassNode node, String name) {
        return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElse(null);
    }

    private static int xaeroArgs(MethodNode method, Type type) {
        int n = 0;
        for (Type arg : Type.getArgumentTypes(method.desc)) if (arg.equals(type)) n++;
        return n;
    }

    private static boolean xaeroInvokes(MethodNode method, String name) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.name.equals(name)) return true;
        }
        return false;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isDisabled(mixinClassName))
            return false;

        if (standsDown(mixinClassName))
            return false;

        if (mixinClassName.contains(".perf.xaero."))
            return xaeroCompatible(targetClassName, mixinClassName);

        if (contains(OPT_IN, mixinClassName) && !contains(ENABLED, mixinClassName))
            return false;

        if (mixinClassName.contains("VulkanBackendList") && !vulkanBackend())
            return false;

        if (mixinClassName.endsWith("MixinVulkanOwoBlur") && !Mods.loaded("owo"))
            return false;

        if (mixinClassName.endsWith("MixinIrisUppercasePaths") && !Mods.loaded("iris"))
            return false;

        if (mixinClassName.endsWith("MixinVulkanBackendListImGui") && !Mods.loaded("flashback") && !Mods.loaded("axiom"))
            return false;

        if (mixinClassName.startsWith(SODIUM_VULKAN_PACKAGE))
            return SODIUM_V08 ? !Arrays.asList(SODIUM_VULKAN_09_ONLY).contains(mixinClassName.substring(SODIUM_VULKAN_PACKAGE.length()))
                : SODIUM_PERF && !mixinClassName.contains("Sodium08");

        if (!mixinClassName.startsWith(SODIUM_PERF_PACKAGE))
            return true;

        if (!SODIUM_PERF)
            return false;

        if (mixinClassName.startsWith(SODIUM_08_PACKAGE))
            return SODIUM_08;

        return !SODIUM_08 || !SODIUM_09_ONLY.contains(mixinClassName);
    }

    @Override
    public void onLoad(String mixinPackage) {
        //? if >= 1.21.11 && < 26.2 {
        /*if (vulkanBackend() && !standsDown("VulkanBackendList") && Mods.loaded("iris"))
            IrisVulkanGate.apply();
        *///?}
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static final String[] SODIUM_GL_HANDLERS = {"sodium$preRender", "sodium$postRender", "sodium$preSwapBuffers", "sodium$postContextReady"};

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith(".skyboxify.MixinSkyFeatureRenderer")) {
            // MixinExtras only wires up @WrapOperation call sites after every postApply, so inspect the class once it's fully transformed
            skyboxifyRenderer = targetClass;
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanServerEntryGl")) {
            for (MethodNode method : targetClass.methods)
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof MethodInsnNode call && call.owner.equals("com/mojang/blaze3d/opengl/GlStateManager")
                        && (call.name.equals("_enableBlend") || call.name.equals("_disableBlend")))
                        call.owner = "org/polyfrost/polydium/vulkan/VulkanActive";
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanBackendListAsyncParticles")) {
            ClassNode renamed = new ClassNode();
            targetClass.accept(new ClassRemapper(renamed, new Remapper() {
                @Override
                public String map(String name) {
                    return name.startsWith("net/vulkanmod/") || name.endsWith("/compat/vulkanmod/RendererAddon")
                        ? "org/polyfrost/polydium/vulkan/AsyncParticlesVulkan$" + name.substring(name.lastIndexOf('/') + 1) : name;
                }
            }));
            targetClass.fields = renamed.fields;
            targetClass.methods = renamed.methods;
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanBackendListDistantHorizons")) {
            //? if >= 26.1 {
            for (MethodNode method : targetClass.methods) {
                if (!method.name.equals("setClipPlanes") || !method.desc.equals("(FF)V"))
                    continue;

                InsnList zeroToOne = new InsnList();
                LabelNode proceed = new LabelNode();
                zeroToOne.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "active", "Z"));
                zeroToOne.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
                for (String field : new String[]{"m22", "m23"}) {
                    zeroToOne.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    zeroToOne.add(new VarInsnNode(Opcodes.FLOAD, 1));
                    zeroToOne.add(new VarInsnNode(Opcodes.FLOAD, 2));
                    zeroToOne.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/polyfrost/polydium/vulkan/DistantHorizonsVulkan", field, "(FF)F"));
                    zeroToOne.add(new FieldInsnNode(Opcodes.PUTFIELD, targetClass.name, field, "F"));
                }
                zeroToOne.add(new InsnNode(Opcodes.RETURN));
                zeroToOne.add(proceed);
                method.instructions.insert(zeroToOne);
            }
            //?}
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanDhLightmap")) {
            for (MethodNode method : targetClass.methods)
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (insn instanceof TypeInsnNode cast && cast.getOpcode() == Opcodes.CHECKCAST && cast.desc.equals(GL_TEXTURE))
                        method.instructions.remove(cast);
                    else if (insn instanceof MethodInsnNode call && call.name.equals(GL_ID) && call.owner.equals(GL_TEXTURE))
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "textureId", "(Ljava/lang/Object;)I"));
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanOwoBlur")) {
            for (MethodNode method : targetClass.methods) {
                if (!method.name.endsWith("drawBlur"))
                    continue;

                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (insn instanceof TypeInsnNode cast && cast.getOpcode() == Opcodes.CHECKCAST && cast.desc.equals(OWO_GL_ENCODER))
                        method.instructions.remove(cast);
                    else if (insn instanceof MethodInsnNode call && call.owner.equals(OWO_GL_ENCODER) && call.name.equals("owo$setInRenderPass"))
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, OWO_VULKAN, "setInRenderPass", "(Ljava/lang/Object;Z)V"));
                    else if (insn instanceof MethodInsnNode call && call.name.equals("copyTextureToTexture") && call.owner.equals("com/mojang/blaze3d/systems/CommandEncoder"))
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, OWO_VULKAN, "copyTextureToTexture", "(L" + call.owner + ";" + call.desc.substring(1)));
            }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanOwoBraid")) {
            for (MethodNode method : targetClass.methods) {
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof MethodInsnNode call && call.owner.equals("org/lwjgl/glfw/GLFW") && call.name.equals("glfwDestroyWindow"))
                        call.owner = OWO_VULKAN;

                InsnList body = new InsnList();
                if (method.name.equals("withContext") && method.desc.equals("(JLjava/lang/Runnable;)V")) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    body.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true));
                } else if (method.name.startsWith("lambda$create$")) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    body.add(new VarInsnNode(Opcodes.ILOAD, 0));
                    body.add(new VarInsnNode(Opcodes.ILOAD, 1));
                    body.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWO_VULKAN, "braidCreateWindow", "(IILjava/lang/String;)J"));
                    body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/apache/commons/lang3/mutable/MutableLong", "setValue", "(J)V"));
                } else if (method.name.startsWith("lambda$recreateLocalFbo$")) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    body.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, "handle", "J"));
                    body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWO_VULKAN, "braidResize", "(J)V"));
                } else if (method.name.startsWith("lambda$endRendering$")) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    body.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, "handle", "J"));
                    body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    body.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, "remoteTarget", "Lcom/mojang/blaze3d/pipeline/TextureTarget;"));
                    body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWO_VULKAN, "braidPresent", "(JLcom/mojang/blaze3d/pipeline/RenderTarget;)V"));
                } else continue;
                body.add(new InsnNode(Opcodes.RETURN));
                method.instructions.insert(vulkanReturn(body));
                method.maxStack = Math.max(method.maxStack, 4);
            }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanStella")) {
            for (MethodNode method : targetClass.methods) {
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof MethodInsnNode call && call.owner.equals("org/lwjgl/opengl/GL33C"))
                        call.owner = STELLA_VULKAN;

                if (!method.name.equals("renderToTexture") || !method.desc.startsWith("(Lco/stellarskys/stella/api/lumina/LuminaPIPRenderer$LuminaRenderState;"))
                    continue;
                InsnList guard = new InsnList();
                LabelNode proceed = new LabelNode();
                guard.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "active", "Z"));
                guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
                guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, STELLA_VULKAN, "renderToTexture", "(Ljava/lang/Object;)V"));
                guard.add(new InsnNode(Opcodes.RETURN));
                guard.add(proceed);
                method.instructions.insert(guard);
            }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanOdin")) {
            boolean companion = targetClass.name.endsWith("$Companion");
            for (MethodNode method : targetClass.methods) {
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof MethodInsnNode call && call.owner.startsWith("org/lwjgl/nanovg/NanoVG") && ODIN_NVG.contains(call.name + call.desc))
                        call.owner = ODIN_VULKAN;

                if ((method.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) != Opcodes.ACC_PUBLIC || method.name.equals("<init>")
                    || (companion ? !method.name.equals("draw") : ODIN_KEEP.contains(method.name)))
                    continue;
                method.instructions.insert(vulkanReturn(odinVulkan(targetClass.name, method)));
                method.maxStack = Math.max(method.maxStack, (Type.getArgumentsAndReturnSizes(method.desc) >> 2) + 1);
            }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanShine")) {
            for (MethodNode method : targetClass.methods)
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (!(insn instanceof MethodInsnNode call)) continue;
                    else if (call.name.equals("resolveDirectStateAccess") && method.name.equals("ensureAttachment"))
                        method.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, SHINE_VULKAN, "attachmentGate", "(Ljava/lang/Object;)Ljava/lang/Object;"));
                    else if (call.owner.equals("org/lwjgl/opengl/GL20") && SHINE_GL.contains(call.name + call.desc))
                        call.owner = SHINE_VULKAN;
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanBackendListImGui")) {
            for (MethodNode method : targetClass.methods) {
                if (!mergedFrom(method).startsWith("com.moulberry."))
                    continue;
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (insn instanceof MethodInsnNode call && isMoulberryGl(call))
                        call.owner = MOULBERRY_VULKAN;
                if (method.name.contains("afterMainBlit") && method.desc.endsWith(")V"))
                    method.instructions.insert(vulkanReturn(new InsnNode(Opcodes.RETURN)));
            }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanImGui")) {
            String mod = targetClass.name.startsWith("com/moulberry/flashback/") ? "flashback" : "axiom";
            for (MethodNode method : targetClass.methods) {
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (!(insn instanceof MethodInsnNode call)) continue;
                    else if (call.name.equals(GL_ID) && call.owner.equals(GL_TEXTURE)) {
                        if (call.getPrevious() instanceof TypeInsnNode cast && cast.getOpcode() == Opcodes.CHECKCAST) method.instructions.remove(cast);
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "textureId", "(Ljava/lang/Object;)I"));
                    } else if (isMoulberryGl(call)) {
                        call.owner = MOULBERRY_VULKAN;
                        if (call.name.equals("glCullFace") && method.name.equals("setupPipeline"))
                            for (AbstractInsnNode ret : method.instructions.toArray())
                                if (ret.getOpcode() == Opcodes.ARETURN) {
                                    method.instructions.insertBefore(ret, new InsnNode(Opcodes.DUP));
                                    method.instructions.insertBefore(ret, new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "tagCullFace", "(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"));
                                }
                    }

                if (method.name.equals("bindDepth") && method.desc.equals("(" + RENDER_TARGET + ")V"))
                    method.instructions.insert(vulkanReturn(new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "bindDepth", "(Ljava/lang/Object;)V"), new InsnNode(Opcodes.RETURN)));

                if (method.name.equals("readDepth") && method.desc.equals("(" + RENDER_TARGET + "IIII)Ljava/util/concurrent/CompletableFuture;"))
                    method.instructions.insert(vulkanReturn(new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2), new VarInsnNode(Opcodes.ILOAD, 3), new VarInsnNode(Opcodes.ILOAD, 4),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "readDepth", "(Ljava/lang/Object;IIII)Ljava/util/concurrent/CompletableFuture;"),
                        new InsnNode(Opcodes.ARETURN)));

                if (targetClass.name.endsWith("/exporting/SaveableFramebuffer")) {
                    String gpuTexture = "Lcom/mojang/blaze3d/textures/GpuTexture;";
                    if (method.name.equals("startDownload") && method.desc.equals("(" + gpuTexture + "II)V"))
                        method.instructions.insert(vulkanReturn(new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1),
                            new VarInsnNode(Opcodes.ILOAD, 2), new VarInsnNode(Opcodes.ILOAD, 3),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "startDownload", "(Ljava/lang/Object;" + gpuTexture + "II)V"),
                            new InsnNode(Opcodes.RETURN)));
                    else if (method.name.equals("finishDownload") && method.desc.startsWith("(II)L"))
                        method.instructions.insert(vulkanReturn(new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ILOAD, 1), new VarInsnNode(Opcodes.ILOAD, 2),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "finishDownload", "(Ljava/lang/Object;II)Ljava/lang/Object;"),
                            new TypeInsnNode(Opcodes.CHECKCAST, Type.getReturnType(method.desc).getInternalName()), new InsnNode(Opcodes.ARETURN)));
                    else if (method.name.equals("close") && method.desc.equals("()V"))
                        method.instructions.insert(vulkanReturn(new VarInsnNode(Opcodes.ALOAD, 0),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "closeDownload", "(Ljava/lang/Object;)V")));
                }

                if (targetClass.name.endsWith("/CustomImGuiImplGl3")) {
                    InsnList body = moulberryRenderer(mod, method);
                    if (body != null)
                        method.instructions.insert(vulkanReturn(body));
                }
            }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanJourneyMap")) {
            for (MethodNode method : targetClass.methods)
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (insn instanceof MethodInsnNode call && call.name.equals(GL_ID) && call.owner.equals(GL_TEXTURE)
                        && call.getPrevious() instanceof TypeInsnNode cast && cast.getOpcode() == Opcodes.CHECKCAST) {
                        method.instructions.remove(cast);
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "textureId", "(Ljava/lang/Object;)I"));
                    }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanXaeroWorldMap")) {
            for (MethodNode method : targetClass.methods)
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (insn instanceof LdcInsnNode ldc && Integer.valueOf(131096).equals(ldc.cst))
                        method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, XAERO_VULKAN, "cacheVersion", "()I"));
                    else if (insn instanceof IntInsnNode minor && minor.getOpcode() == Opcodes.BIPUSH && minor.operand == 24 && method.name.equals("loadCacheTextures")
                        && minor.getNext().getOpcode() == Opcodes.IF_ICMPGT && minor.getPrevious().getPrevious().getOpcode() == Opcodes.IF_ICMPGT
                        && minor.getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.ICONST_2) {
                        method.instructions.set(minor.getPrevious().getPrevious().getPrevious(), new MethodInsnNode(Opcodes.INVOKESTATIC, XAERO_VULKAN, "cacheMajor", "()I"));
                        method.instructions.set(minor, new MethodInsnNode(Opcodes.INVOKESTATIC, XAERO_VULKAN, "cacheMinor", "()I"));
                    } else if (insn instanceof MethodInsnNode level && level.name.equals("getLevel") && method.name.equals("readCacheData")
                        && level.getNext().getOpcode() == Opcodes.IFNE && level.getNext().getNext().getNext().getOpcode() == Opcodes.IFEQ) {
                        LabelNode blank = new LabelNode();
                        method.instructions.insert(level.getNext().getNext().getNext(), blank);
                        InsnList old = new InsnList();
                        old.add(new VarInsnNode(Opcodes.ILOAD, 2));
                        old.add(new MethodInsnNode(Opcodes.INVOKESTATIC, XAERO_VULKAN, "ignoreCache", "(I)Z"));
                        old.add(new JumpInsnNode(Opcodes.IFNE, blank));
                        method.instructions.insertBefore(level.getPrevious(), old);
                    }
            return;
        }

        if (mixinClassName.endsWith("MixinVulkanEssential") || mixinClassName.endsWith("MixinVulkanUniversalCraft")) {
            boolean universalCraft = targetClass.name.contains("/universal/render/");
            for (MethodNode method : targetClass.methods) {
                boolean guiSetup = targetClass.name.equals("gg/essential/util/GuiRenderSetupKt");
                for (AbstractInsnNode insn : method.instructions.toArray())
                    if (!(insn instanceof MethodInsnNode call)) continue;
                    else if (!guiSetup && call.name.equals("isZZeroToOne") && call.owner.equals("gg/essential/util/GuiEssentialPlatform"))
                        method.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "isZZeroToOne", "(Z)Z"));
                    else if (guiSetup && call.owner.equals("org/joml/Matrix4f") && call.name.equals("get") && call.desc.equals("(Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;"))
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "guiProjection", "(Lorg/joml/Matrix4f;Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;"));

                InsnList body = universalCraft ? universalCraftVulkan(targetClass.name, method) : essentialVulkan(targetClass.name, method);
                if (body == null) continue;
                InsnList guard = new InsnList();
                LabelNode proceed = new LabelNode();
                guard.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "active", "Z"));
                guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
                guard.add(body);
                guard.add(proceed);
                method.instructions.insert(guard);
            }
            return;
        }

        if (!mixinClassName.endsWith("MixinVulkanRenderAhead") && !mixinClassName.endsWith("MixinVulkanSodiumGuard"))
            return;

        for (MethodNode method : targetClass.methods) {
            if (!method.desc.endsWith(")V") || Arrays.stream(SODIUM_GL_HANDLERS).noneMatch(method.name::contains))
                continue;

            InsnList guard = new InsnList();
            LabelNode proceed = new LabelNode();
            guard.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "active", "Z"));
            guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
            guard.add(new InsnNode(Opcodes.RETURN));
            guard.add(proceed);
            method.instructions.insert(guard);
        }
    }

    private static final String OWO_VULKAN = "org/polyfrost/polydium/vulkan/OwoVulkan";
    private static final String OWO_GL_ENCODER = "io/wispforest/owo/mixin/ui/access/GlCommandEncoderAccessor";
    private static final String XAERO_VULKAN ="org/polyfrost/polydium/vulkan/XaeroVulkan";
    private static final String UC_VULKAN = "org/polyfrost/polydium/vulkan/UniversalCraftVulkan";
    private static final String STELLA_VULKAN = "org/polyfrost/polydium/vulkan/StellaVulkan";
    private static final String SHINE_VULKAN = "org/polyfrost/polydium/vulkan/ShineVulkan";
    private static final Set<String> SHINE_GL = Set.of("glGetUniformLocation(ILjava/lang/CharSequence;)I", "glUniform1i(II)V", "glUniform4fv(ILjava/nio/FloatBuffer;)V");
    private static final String MOULBERRY_VULKAN = "org/polyfrost/polydium/vulkan/MoulberryVulkan";
    private static final Set<String> MOULBERRY_GL = Set.of("glGetInteger(I)I", "_glBindFramebuffer(II)V", "_viewport(IIII)V",
        "glGetIntegerv(I[I)V", "_bindTexture(I)V", "glTexParameteri(III)V", "_deleteTexture(I)V", "glCullFace(I)V", "glReadPixels(IIIIII[F)V",
        "glGenQueries()I", "glBeginQuery(II)V", "glEndQuery(I)V", "glGetQueryObjecti(II)I", "glDeleteQueries(I)V");

    private static boolean isMoulberryGl(MethodInsnNode call) {
        return (call.owner.startsWith("org/lwjgl/opengl/GL") || call.owner.equals("com/mojang/blaze3d/opengl/GlStateManager"))
            && MOULBERRY_GL.contains(call.name + call.desc);
    }

    private static InsnList moulberryRenderer(String mod, MethodNode method) {
        Type ret = Type.getReturnType(method.desc);
        InsnList body = new InsnList();
        switch (method.name + method.desc.substring(0, method.desc.indexOf(')') + 1)) {
            case "init()", "init(Ljava/lang/String;)", "newFrame()", "updateFontsTexture()", "destroyFontsTexture()" -> {
                body.add(new LdcInsnNode(mod));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, method.name, "(Ljava/lang/String;)V"));
            }
            case "shutdown()", "dispose()" -> {
                body.add(new LdcInsnNode(mod));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "destroyFontsTexture", "(Ljava/lang/String;)V"));
            }
            case "getTextureId(Lcom/mojang/blaze3d/textures/GpuTextureView;)" -> {
                body.add(new VarInsnNode(Opcodes.ALOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "getTextureId", "(Ljava/lang/Object;)J"));
            }
            case "setSampleNearest(J)", "setSampleLinear(J)" -> {
                body.add(new VarInsnNode(Opcodes.LLOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, method.name, "(J)V"));
            }
            default -> {
                if (!method.name.equals("renderDrawData") || Type.getArgumentTypes(method.desc).length != 1)
                    return null;
                body.add(new LdcInsnNode(mod));
                body.add(new VarInsnNode(Opcodes.ALOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MOULBERRY_VULKAN, "renderDrawData", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"));
                body.add(ret.getSort() == Type.VOID ? new InsnNode(Opcodes.POP) : new TypeInsnNode(Opcodes.CHECKCAST, ret.getInternalName()));
            }
        }
        if (ret.getSort() == Type.BOOLEAN)
            body.add(new InsnNode(Opcodes.ICONST_1));
        body.add(new InsnNode(ret.getOpcode(Opcodes.IRETURN)));
        return body;
    }

    private static final String ODIN_VULKAN = "org/polyfrost/polydium/vulkan/OdinVulkan";
    private static final String ODIN_IMAGE = "Lcom/odtheking/odin/utils/ui/rendering/Image;";
    private static final Set<String> ODIN_NVG = Set.of("nvgCreate(I)J", "nvgCreateImageRGBA(JIIILjava/nio/ByteBuffer;)I", "nvgDeleteImage(JI)V");
    private static final Set<String> ODIN_KEEP = Set.of("createImage", "deleteImage", "devicePixelRatio", "getDefaultFont");
    private static final Set<String> ODIN_NOOP = Set.of("beginFrame(FF)V", "endFrame()V", "globalAlpha(F)V", "createNVGImage(III)I", "image(IIIIIIIFFFFF)V");

    private static InsnList odinVulkan(String owner, MethodNode method) {
        InsnList body = new InsnList();
        Type ret = Type.getReturnType(method.desc);
        if (ODIN_NOOP.contains(method.name + method.desc)) {
            if (ret.getSort() == Type.INT) body.add(new InsnNode(Opcodes.ICONST_0));
            body.add(new InsnNode(ret.getOpcode(Opcodes.IRETURN)));
            return body;
        }

        StringBuilder desc = new StringBuilder("(");
        int slot = 1;
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            if (arg.getDescriptor().equals(ODIN_IMAGE)) {
                body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                body.add(new VarInsnNode(Opcodes.ALOAD, slot));
                body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, owner, "getImage", "(" + ODIN_IMAGE + ")I"));
                desc.append('I');
            } else {
                body.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), slot));
                boolean foreign = arg.getSort() == Type.OBJECT && (arg.getInternalName().startsWith("com/odtheking/") || arg.getInternalName().startsWith("kotlin/"));
                desc.append(foreign ? "Ljava/lang/Object;" : arg.getDescriptor());
            }
            slot += arg.getSize();
        }
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ODIN_VULKAN, method.name, desc.append(')').append(ret.getDescriptor()).toString()));
        body.add(new InsnNode(ret.getOpcode(Opcodes.IRETURN)));
        return body;
    }

    private static InsnList vulkanReturn(AbstractInsnNode... body) {
        InsnList list = new InsnList();
        for (AbstractInsnNode insn : body)
            list.add(insn);
        return vulkanReturn(list);
    }

    private static InsnList vulkanReturn(InsnList body) {
        InsnList guard = new InsnList();
        LabelNode proceed = new LabelNode();
        guard.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/polyfrost/polydium/vulkan/VulkanActive", "active", "Z"));
        guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
        guard.add(body);
        guard.add(proceed);
        return guard;
    }

    private static String mergedFrom(MethodNode method) {
        for (List<AnnotationNode> annotations : Arrays.asList(method.visibleAnnotations, method.invisibleAnnotations))
            if (annotations != null)
                for (AnnotationNode annotation : annotations)
                    if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"))
                        for (int i = 0; i + 1 < annotation.values.size(); i += 2)
                            if (annotation.values.get(i).equals("mixin"))
                                return (String) annotation.values.get(i + 1);
        return "";
    }

    private static InsnList universalCraftVulkan(String owner, MethodNode method) {
        String r = owner.substring(0, owner.lastIndexOf('/') + 1);
        InsnList body = new InsnList();
        switch (method.name + method.desc.replace(r, "")) {
            case "isZZeroToOne()Z" -> {
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "isZZeroToOne", "()Z"));
                body.add(new InsnNode(Opcodes.IRETURN));
            }
            case "maxTextureSize(LUGpuFormat;)I" -> {
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "maxTextureSize", "()I"));
                body.add(new InsnNode(Opcodes.IRETURN));
            }
            case "createGlTexture(Ljava/lang/String;LUGpuTexture$Usage;LUGpuFormatImpl;III)LUGpuTextureImpl;" -> {
                for (int slot = 0; slot <= 3; slot++) body.add(new VarInsnNode(Opcodes.ALOAD, slot));
                body.add(new VarInsnNode(Opcodes.ALOAD, 3));
                body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, r + "UGpuFormatImpl", "getInternalFormat", "()I"));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "textureFormat", "(I)Lcom/mojang/blaze3d/textures/TextureFormat;"));
                for (int slot = 4; slot <= 6; slot++) body.add(new VarInsnNode(Opcodes.ILOAD, slot));
                body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, owner, "createB3DTexture",
                    "(Ljava/lang/String;L" + r + "UGpuTexture$Usage;L" + r + "UGpuFormatImpl;Lcom/mojang/blaze3d/textures/TextureFormat;III)L" + r + "UGpuTextureImpl;"));
                body.add(new InsnNode(Opcodes.ARETURN));
            }
            case "clearColor(LUGpuTexture;LURenderPassDescriptor$RenderArea;LURenderPassDescriptor$ClearColor;)V" -> {
                body.add(universalCraftMc(r, "UGpuTextureImpl", 1, "Lcom/mojang/blaze3d/textures/GpuTexture;"));
                for (String channel : new String[]{"getRed", "getGreen", "getBlue", "getAlpha"}) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, r + "URenderPassDescriptor$ClearColor", channel, "()F"));
                }
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "clearColor", "(Lcom/mojang/blaze3d/textures/GpuTexture;FFFF)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "clearDepth(LUGpuTexture;LURenderPassDescriptor$RenderArea;D)V" -> {
                body.add(universalCraftMc(r, "UGpuTextureImpl", 1, "Lcom/mojang/blaze3d/textures/GpuTexture;"));
                body.add(new VarInsnNode(Opcodes.DLOAD, 3));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "clearDepth", "(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "copyBufferToTexture(LUGpuBufferSlice;IIIILUGpuTexture;IIIIII)V" -> {
                body.add(new VarInsnNode(Opcodes.ALOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, r + "UGpuBufferSlice", "getBuffer", "()L" + r + "UGpuBuffer;"));
                body.add(new TypeInsnNode(Opcodes.CHECKCAST, r + "UGpuBufferImpl"));
                body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, r + "UGpuBufferImpl", "getMc", "()Lcom/mojang/blaze3d/buffers/GpuBuffer;"));
                for (String range : new String[]{"getOffset", "getSize"}) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, r + "UGpuBufferSlice", range, "()J"));
                }
                for (int slot = 2; slot <= 5; slot++) body.add(new VarInsnNode(Opcodes.ILOAD, slot));
                body.add(universalCraftMc(r, "UGpuTextureImpl", 6, "Lcom/mojang/blaze3d/textures/GpuTexture;"));
                for (int slot = 7; slot <= 12; slot++) body.add(new VarInsnNode(Opcodes.ILOAD, slot));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "copyBufferToTexture",
                    "(Lcom/mojang/blaze3d/buffers/GpuBuffer;JJIIIILcom/mojang/blaze3d/textures/GpuTexture;IIIIII)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "glFenceSync(II)Ljava/lang/Long;" -> {
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "fenceCreate", "()Ljava/lang/Long;"));
                body.add(new InsnNode(Opcodes.ARETURN));
            }
            case "glClientWaitSync(JIJ)I" -> {
                body.add(new VarInsnNode(Opcodes.LLOAD, 0));
                body.add(new VarInsnNode(Opcodes.LLOAD, 3));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "fenceWait", "(JJ)I"));
                body.add(new InsnNode(Opcodes.IRETURN));
            }
            case "glDeleteSync(J)V" -> {
                body.add(new VarInsnNode(Opcodes.LLOAD, 0));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UC_VULKAN, "fenceDelete", "(J)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            default -> {
                return null;
            }
        }
        return body;
    }

    private static InsnList universalCraftMc(String r, String impl, int slot, String mcDesc) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, slot));
        list.add(new TypeInsnNode(Opcodes.CHECKCAST, r + impl));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, r + impl, "getMc", "()" + mcDesc));
        return list;
    }

    private static final String ESSENTIAL_VULKAN = "org/polyfrost/polydium/vulkan/EssentialVulkan";
    private static final String MC_TEXTURE = "Lcom/mojang/blaze3d/textures/GpuTexture;";

    private static InsnList essentialVulkan(String owner, MethodNode method) {
        InsnList body = new InsnList();
        switch (owner.substring(owner.lastIndexOf('/') + 1) + "." + method.name + method.desc) {
            case "MinecraftWindowedTextureProviderKt.makeUploadBackend()Lgg/essential/gui/screenshot/providers/UploadBackend;" -> {
                String backend = "gg/essential/gui/screenshot/providers/NotAsyncUploadBackend";
                body.add(new TypeInsnNode(Opcodes.NEW, backend));
                body.add(new InsnNode(Opcodes.DUP));
                body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, backend, "<init>", "()V"));
                body.add(new InsnNode(Opcodes.ARETURN));
            }
            case "PixelBufferTexture.upload(Lgg/essential/gui/screenshot/downsampling/PixelBuffer;)V" -> {
                body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, "getGlTexture", "()" + MC_TEXTURE));
                body.add(new VarInsnNode(Opcodes.ALOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "gg/essential/gui/screenshot/downsampling/PixelBuffer", "prepareDirectBuffer", "()Ljava/nio/ByteBuffer;"));
                for (String size : new String[]{"getImageWidth", "getImageHeight"}) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, size, "()I"));
                }
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "upload", "(" + MC_TEXTURE + "Ljava/nio/ByteBuffer;II)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "GpuTextureImpl.clearColor-EIFkdBU(I)V" -> {
                body.add(essentialB3d());
                for (String channel : new String[]{"getR", "getG", "getB", "getA"}) {
                    body.add(new VarInsnNode(Opcodes.ILOAD, 1));
                    body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "gg/essential/model/util/Color", channel + "-w2LRezQ", "(I)B"));
                }
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "clearColor", "(" + MC_TEXTURE + "BBBB)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "GpuTextureImpl.clearDepth(F)V" -> {
                body.add(essentialB3d());
                body.add(new VarInsnNode(Opcodes.FLOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "clearDepth", "(" + MC_TEXTURE + "F)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "GpuTextureImpl.copyFrom(Ljava/lang/Iterable;)V" -> {
                body.add(essentialB3d());
                body.add(new VarInsnNode(Opcodes.ALOAD, 1));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "copyFrom", "(" + MC_TEXTURE + "Ljava/lang/Iterable;)V"));
                body.add(new InsnNode(Opcodes.RETURN));
            }
            case "GpuTextureImpl.readPixelColors(IIII)Lgg/essential/util/image/bitmap/Bitmap;" -> {
                String bitmap = owner + "$ByteBufferBitmap";
                body.add(new TypeInsnNode(Opcodes.NEW, bitmap));
                body.add(new InsnNode(Opcodes.DUP));
                body.add(new VarInsnNode(Opcodes.ILOAD, 3));
                body.add(new VarInsnNode(Opcodes.ILOAD, 4));
                body.add(essentialB3d());
                for (int slot = 1; slot <= 4; slot++) body.add(new VarInsnNode(Opcodes.ILOAD, slot));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "readPixelColors", "(" + MC_TEXTURE + "IIII)Ljava/nio/ByteBuffer;"));
                body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, bitmap, "<init>", "(IILjava/nio/ByteBuffer;)V"));
                body.add(new InsnNode(Opcodes.ARETURN));
            }
            case "GpuTextureImpl.readPixelDepths(IIII)Ljava/nio/FloatBuffer;" -> {
                body.add(essentialB3d());
                for (int slot = 1; slot <= 4; slot++) body.add(new VarInsnNode(Opcodes.ILOAD, slot));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ESSENTIAL_VULKAN, "readPixelDepths", "(" + MC_TEXTURE + "IIII)Ljava/nio/FloatBuffer;"));
                body.add(new InsnNode(Opcodes.ARETURN));
            }
            default -> {
                return null;
            }
        }
        return body;
    }

    private static InsnList essentialB3d() {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "gg/essential/universal/UGraphics", "getPlatformAdapter", "()Lgg/essential/universal/render/UGpuPlatformAdapter;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "gg/essential/util/image/GpuTexture", "getUc", "()Lgg/essential/universal/render/UGpuTexture;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "gg/essential/universal/render/UGpuPlatformAdapter", "texture", "(Lgg/essential/universal/render/UGpuTexture;)" + MC_TEXTURE));
        return list;
    }
}
