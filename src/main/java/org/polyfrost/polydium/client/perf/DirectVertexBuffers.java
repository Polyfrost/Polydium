package org.polyfrost.polydium.client.perf;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.fabricmc.loader.api.FabricLoader;
import org.polyfrost.polydium.mixin.client.perf.ParticleBufferBuilderAccessor;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class DirectVertexBuffers {
    private static final String SODIUM_MIXINS = "net.caffeinemc.mods.sodium.";
    private static final String OWN_MIXINS = "org.polyfrost.polydium.";
    private static final String IRIS_MIXINS = "net.irisshaders.iris.mixin.vertices.";
    //? if >= 1.21.11 {
    private static final List<String> IRIS_VERSIONS = List.of("1.10.8", "1.11.4", "1.11.7");
    //?} else {
    /*private static final List<String> IRIS_VERSIONS = List.of("1.8.8", "1.8.11", "1.8.14", "1.9.6", "1.9.7");
    *///?}
    private static final Set<String> HARMLESS_MIXINS = Set.of(
            "xaeroplus.mixin.client.mc.MixinBufferBuilder",
            "net.diebuddies.mixins.iris.MixinBufferBuilder",
            "fi.dy.masa.litematica.mixin.render.MixinBufferBuilder"
    );

    private static final Pattern HANDLER = Pattern.compile("^\\w+\\$[a-z]{3}\\w{3}\\$.*");

    private static int state;

    private DirectVertexBuffers() {
    }

    public static long reserve(Object writer, VertexFormat format, int stride, int vertices) {
        if (writer.getClass() != BufferBuilder.class || !supported()) {
            return 0L;
        }
        ParticleBufferBuilderAccessor target = (ParticleBufferBuilderAccessor) writer;
        if (target.polydium$format() != format || target.polydium$vertexSize() != stride) {
            return 0L;
        }
        int bytes = vertices * stride;
        long pointer = target.polydium$buffer().reserve(bytes);
        target.polydium$setVertices(target.polydium$vertices() + vertices);
        target.polydium$setVertexPointer(pointer + bytes - stride);
        target.polydium$setElementsToFill(0);
        return pointer;
    }

    public static VertexFormat formatOf(Object writer) {
        return writer instanceof BufferBuilder ? ((ParticleBufferBuilderAccessor) writer).polydium$format() : null;
    }

    public static boolean harmless(String mixin, Method method) {
        return mixin.startsWith(OWN_MIXINS)
                || HARMLESS_MIXINS.contains(mixin)
                || addsOnly(method.getName())
                || accessorOnly(mixin)
                || mixin.startsWith(IRIS_MIXINS) && Iris.KNOWN;
    }

    public static boolean addsOnly(String name) {
        return name.indexOf('$') > 0 && !HANDLER.matcher(name).matches();
    }

    private static boolean accessorOnly(String mixin) {
        try {
            ClassInfo info = ClassInfo.forName(mixin);
            return info != null && info.isInterface();
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean supported() {
        if (state == 0) {
            state = scan() ? 1 : -1;
        }
        return state > 0;
    }

    private static boolean scan() {
        boolean sodium = false;
        try {
            for (Method method : BufferBuilder.class.getDeclaredMethods()) {
                MixinMerged merged = method.getAnnotation(MixinMerged.class);
                if (merged == null) {
                    continue;
                }
                String mixin = merged.mixin();
                if (mixin.startsWith(SODIUM_MIXINS)) {
                    sodium = true;
                } else if (!harmless(mixin, method)) {
                    LoggerFactory.getLogger("polydium/perf").info("Direct vertex writes disabled: BufferBuilder is modified by {}", mixin);
                    return false;
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return sodium;
    }

    private static final class Iris {
        static final boolean KNOWN = FabricLoader.getInstance().getModContainer("iris")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .map(version -> IRIS_VERSIONS.stream().anyMatch(version::startsWith))
                .orElse(false);
    }
}
