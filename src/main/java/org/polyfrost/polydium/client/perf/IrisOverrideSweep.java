package org.polyfrost.polydium.client.perf;

//? if >= 26.3 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class IrisOverrideSweep {
    private static final Logger LOGGER = LoggerFactory.getLogger("polydium/perf");
    private static final Set<Object> DESTROYED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean disabled;

    private IrisOverrideSweep() {
    }

    public static void destroying(Object pipeline) {
        if (disabled) {
            return;
        }

        try {
            Api.collectPrograms(pipeline, DESTROYED);
        } catch (Throwable e) {
            disable(e);
        }
    }

    public static void tick() {
        if (DESTROYED.isEmpty()) {
            return;
        }

        try {
            Api.removeLivePrograms(DESTROYED);
            sweep(overrides());
        } catch (Throwable e) {
            disable(e);
        } finally {
            DESTROYED.clear();
        }
    }

    private static void sweep(Map<?, ?> overrides) {
        for (Object byProgram : overrides.values()) {
            Iterator<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) byProgram).entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<?, ?> entry = entries.next();
                if (!DESTROYED.contains(entry.getKey())) {
                    continue;
                }

                List<?> pipelines;
                try {
                    pipelines = new ArrayList<>(((Map<?, ?>) entry.getValue()).values());
                    entries.remove();
                } catch (RuntimeException e) {
                    LOGGER.warn("Failed to read a stale Iris override", e);
                    continue;
                }

                for (Object pipeline : pipelines) {
                    try {
                        ((GlRenderPipeline) ((FrontendRenderPipeline) pipeline).backendRenderPipeline()).vertexArray().close();
                    } catch (RuntimeException e) {
                        LOGGER.warn("Failed to close a stale Iris override", e);
                    }
                }
            }
        }
    }

    private static Map<?, ?> overrides() throws IllegalAccessException, NoSuchFieldException {
        for (Field field : RenderSystem.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().contains("iris$overrides") && Map.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return (Map<?, ?>) field.get(null);
            }
        }

        throw new NoSuchFieldException("iris$overrides");
    }

    private static void disable(Throwable cause) {
        disabled = true;
        LOGGER.info("Iris override sweep disabled: {}", cause.toString());
    }

    private static final class Api {
        static void collectPrograms(Object pipeline, Set<Object> into) {
            if (!(pipeline instanceof IrisRenderingPipeline iris)) {
                return;
            }

            ShaderMap shaders = iris.getShaderMap();
            for (ShaderKey key : ShaderKey.values()) {
                GlProgram program = shaders.getShader(key);
                if (program != null) {
                    into.add(program);
                }
            }
        }

        static void removeLivePrograms(Set<Object> programs) throws IllegalAccessException, NoSuchFieldException {
            Set<Object> live = Collections.newSetFromMap(new IdentityHashMap<>());
            PipelineManager manager = Iris.getPipelineManager();
            collectPrograms(manager.getPipelineNullable(), live);

            Field pipelines = PipelineManager.class.getDeclaredField("pipelinesPerDimension");
            pipelines.setAccessible(true);
            for (Object pipeline : ((Map<?, ?>) pipelines.get(manager)).values()) {
                collectPrograms(pipeline, live);
            }

            programs.removeAll(live);
        }
    }
}
//?}
