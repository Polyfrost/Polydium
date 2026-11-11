package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWAllocateCallback;
import org.lwjgl.glfw.GLFWAllocateCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.oneconfig.api.notifications.v1.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Constructor;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

// Mods written for OpenGL call LWJGL's bindings directly, which aborts the JVM when no context is current. The render
// thread gets capabilities made of stubs instead: a few queries are answered from the Vulkan device and every other
// function returns 0.
public final class RawGl {
    private static final Logger LOGGER = LoggerFactory.getLogger("polydium/vulkan");
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final String TOAST_TITLE = "polydium.rawGl.title";

    // Minecraft's pipelines and render passes own this state on OpenGL too, so losing a mod's stray write changes nothing
    private static final Set<String> PIPELINE_STATE = Set.of("glEnable", "glDisable", "glBlendFunc", "glBlendFuncSeparate",
        "glDepthFunc", "glDepthMask", "glColorMask", "glPolygonMode", "glPolygonOffset", "glScissor", "glViewport",
        "glFinish", "glFlush", "glGetError");

    // only the render thread has these capabilities, so none of this state is shared
    private static final Set<String> reported = new HashSet<>();
    private static final Set<String> toasted = new HashSet<>();
    private static final List<String> pendingToasts = new ArrayList<>();
    private static final Map<Integer, Long> strings = new HashMap<>();
    private static long calls;

    private RawGl() {
    }

    public static void install() {
        try {
            long noop = stub((a, b) -> {
                report(false);
                return 0L;
            });
            long getString = stub((name, b) -> {
                long string = string((int) name);
                report(string != 0L);
                return string;
            });
            long getIntegerv = stub((pname, data) -> {
                report(integers((int) pname, data));
                return 0L;
            });
            FunctionProvider functions = name -> switch (MemoryUtil.memASCII(MemoryUtil.memAddress(name))) {
                case "glGetString" -> getString;
                case "glGetIntegerv" -> getIntegerv;
                default -> noop;
            };
            // LWJGL only resolves the functions of versions and extensions it is told are present
            Set<String> everything = new AbstractSet<>() {
                @Override
                public boolean contains(Object extension) {
                    return true;
                }

                @Override
                public Iterator<String> iterator() {
                    return Collections.emptyIterator();
                }

                @Override
                public int size() {
                    return 0;
                }
            };
            Constructor<GLCapabilities> constructor = GLCapabilities.class.getDeclaredConstructor(FunctionProvider.class, Set.class, boolean.class, IntFunction.class);
            constructor.setAccessible(true);
            GL.setCapabilities(constructor.newInstance(functions, everything, false, (IntFunction<PointerBuffer>) BufferUtils::createPointerBuffer));
            ClientTickEvents.END_CLIENT_TICK.register(client -> showToasts());
        } catch (Throwable t) {
            LOGGER.warn("Mods that call OpenGL directly will crash the game on the Vulkan backend", t);
        }
    }

    // The allocator callback is the one LWJGL exposes on every supported version that returns a pointer, and its two
    // arguments are the first two of whichever OpenGL function it stands in for; the caller cleans up any others.
    private static long stub(GLFWAllocateCallbackI function) {
        return GLFWAllocateCallback.create(function).address();
    }

    private static long string(int name) {
        String value = switch (name) {
            case GL11C.GL_VENDOR -> VulkanDevice.active.getVendor();
            case GL11C.GL_RENDERER -> VulkanDevice.active.getRenderer();
            case GL11C.GL_VERSION -> VulkanDevice.active.getVersion();
            default -> null;
        };
        // never freed, the same as a driver's strings
        return value == null ? 0L : strings.computeIfAbsent(name, key -> MemoryUtil.memAddress(MemoryUtil.memASCII(value)));
    }

    private static boolean integers(int pname, long data) {
        switch (pname) {
            case GL11C.GL_MAX_TEXTURE_SIZE -> MemoryUtil.memPutInt(data, VulkanDevice.active.getMaxTextureSize());
            case GL11C.GL_VIEWPORT, GL11C.GL_SCISSOR_BOX -> {
                Window window = Minecraft.getInstance().getWindow();
                MemoryUtil.memPutInt(data, 0);
                MemoryUtil.memPutInt(data + 4, 0);
                MemoryUtil.memPutInt(data + 8, window.getWidth());
                MemoryUtil.memPutInt(data + 12, window.getHeight());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void report(boolean answered) {
        long call = calls++;
        // shortcut: only the first calls and then one in 1021 are traced to a mod, because walking the stack takes
        // microseconds; a mod with few calls among another's many can go unreported, trace every call if that matters.
        // 1021 is prime so the sample doesn't settle on one position in a sequence that repeats every frame.
        if (call >= 256 && call % 1021 != 0)
            return;

        try {
            List<StackFrame> frames = WALKER.walk(stack -> stack.dropWhile(frame -> !frame.getClassName().startsWith("org.lwjgl.opengl.")).limit(16).toList());
            StackFrame caller = frames.stream()
                .filter(frame -> !frame.getClassName().startsWith("org.lwjgl.") && !frame.getClassName().startsWith("com.mojang.blaze3d.opengl."))
                .findFirst().orElse(null);
            if (caller == null)
                return;

            // LWJGL's pointer-taking variants are named after the function they share
            String function = frames.get(0).getMethodName();
            if (function.startsWith("ngl"))
                function = function.substring(1);
            if (!reported.add(function + ' ' + caller.getClassName()))
                return;

            ModContainer mod = mod(caller);
            String name = mod == null ? caller.getClassName() : mod.getMetadata().getName();
            String site = caller.getClassName() + '.' + caller.getMethodName();
            if (answered || PIPELINE_STATE.contains(function)) {
                LOGGER.warn("{} called {} directly in {}; handled without OpenGL on the Vulkan backend", name, function, site);
            } else {
                LOGGER.warn("{} called {} directly in {}; it does nothing on the Vulkan backend, so the mod may render incorrectly", name, function, site);
                if (toasted.add(name))
                    pendingToasts.add(name);
            }
        } catch (Throwable t) {
            // must not unwind into the native caller
            LOGGER.error("Failed to report a raw OpenGL call", t);
        }
    }

    private static @Nullable ModContainer mod(StackFrame caller) {
        String owner = caller.getClassName();
        try {
            // a mixin's code runs from the class it was merged into
            MixinMerged merged = caller.getDeclaringClass().getDeclaredMethod(caller.getMethodName(), caller.getMethodType().parameterArray()).getAnnotation(MixinMerged.class);
            if (merged != null)
                owner = merged.mixin();
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }

        String path = owner.replace('.', '/') + ".class";
        for (ModContainer mod : FabricLoader.getInstance().getAllMods())
            if (mod.findPath(path).isPresent())
                return mod;
        return null;
    }

    private static void showToasts() {
        // the first calls can come before translations have loaded
        if (pendingToasts.isEmpty() || !I18n.exists(TOAST_TITLE))
            return;
        for (String mod : pendingToasts)
            Notifications.info(I18n.get(TOAST_TITLE, mod), I18n.get("polydium.rawGl.message"), 10_000F);
        pendingToasts.clear();
    }
}
