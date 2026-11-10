package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.compat.Mods;
import org.slf4j.LoggerFactory;

public final class EtfStateReuse {
    private static final boolean ETF = Mods.loaded("entity_texture_features");
    private static final String ETF_PACKAGE = "traben.entity_texture_features.";
    private static final String ETF_STATE = "traben.entity_texture_features.features.state.ETFEntityRenderStateViaReference";
    private static final String EMF_STATE = "traben.entity_model_features.models.animation.state.EMFEntityRenderStateViaReference";
    private static final MethodType RESET = MethodType.methodType(void.class, Object.class);
    private static final ClassValue<MethodHandle> RESETTERS = new ClassValue<>() {
        @Override
        protected MethodHandle computeValue(Class<?> type) {
            return type.getName().equals(ETF_STATE) || type.getName().equals(EMF_STATE) ? resetter(type) : null;
        }
    };

    private static boolean tried;
    private static Object original;
    private static MethodHandle originalMake;
    private static Entity reusedEntity;
    private static Entity trackedEntity;

    private EtfStateReuse() {
    }

    static void expect(Entity entity, boolean reused) {
        if (originalMake == null) {
            reusedEntity = trackedEntity = null;
            return;
        }
        reusedEntity = reused ? entity : null;
        trackedEntity = reused ? null : entity;
    }

    static void clear() {
        reusedEntity = trackedEntity = null;
    }

    static void install() {
        if (tried || !ETF)
            return;
        tried = true;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            ClassLoader loader = EtfStateReuse.class.getClassLoader();
            Class<?> etf = Class.forName(ETF_PACKAGE + "ETF", true, loader);
            Class<?> init = Class.forName(ETF_PACKAGE + "features.state.ETFEntityRenderState$ETFRenderStateInit", true, loader);
            Class<?> etfEntity = Class.forName(ETF_PACKAGE + "utils.ETFEntity", true, loader);
            Class<?> state = Class.forName(ETF_PACKAGE + "features.state.ETFEntityRenderState", true, loader);
            Field field = etf.getField("etfRenderStateConstructor");
            Object current = field.get(null);
            if (current == null)
                return;
            originalMake = lookup.findVirtual(init, "make", MethodType.methodType(state, etfEntity))
                .asType(MethodType.methodType(Object.class, Object.class, Object.class));
            original = current;
            MethodType makeType = MethodType.methodType(state, etfEntity);
            MethodHandle make = lookup.findStatic(EtfStateReuse.class, "make", MethodType.methodType(Object.class, Object.class));
            // MethodHandleProxies allocates an argument array per call before Java 22
            field.set(null, LambdaMetafactory.metafactory(lookup, "make", MethodType.methodType(init), makeType, make, makeType).getTarget().invoke());
        } catch (Throwable e) {
            originalMake = null;
            LoggerFactory.getLogger("polydium/perf").info("ETF state reuse disabled: {}", e.toString());
        }
    }

    private static Object make(Object entity) throws Throwable {
        if (entity == null || (entity != reusedEntity && entity != trackedEntity) || !RenderSystem.isOnRenderThread())
            return originalMake.invokeExact(original, entity);
        boolean reused = entity == reusedEntity;
        reusedEntity = trackedEntity = null;
        EntityStateReuse.Holder holder = (EntityStateReuse.Holder) entity;
        Object state = holder.polydium$etfState();
        if (reused && state != null) {
            MethodHandle reset = RESETTERS.get(state.getClass());
            if (reset != null) {
                reset.invokeExact(state);
                return state;
            }
        }
        state = originalMake.invokeExact(original, entity);
        holder.polydium$setEtfState(state != null && RESETTERS.get(state.getClass()) != null ? state : null);
        return state;
    }

    private static MethodHandle resetter(Class<?> type) {
        try {
            Constructor<?> constructor = null;
            for (Constructor<?> candidate : type.getConstructors()) {
                if (candidate.getParameterCount() == 1)
                    constructor = candidate;
            }
            if (constructor == null)
                return null;
            Object template = constructor.newInstance((Object) null);
            MethodType copyType = MethodType.methodType(void.class, Object.class, Object.class);
            MethodHandle copyAll = MethodHandles.empty(copyType);
            for (Class<?> owner = type; owner != Object.class; owner = owner.getSuperclass()) {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, MethodHandles.lookup());
                for (Field field : owner.getDeclaredFields()) {
                    if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) != 0)
                        continue;
                    MethodHandle copy = MethodHandles.filterArguments(lookup.unreflectSetter(field), 1, lookup.unreflectGetter(field));
                    copyAll = MethodHandles.foldArguments(copy.asType(copyType), copyAll);
                }
            }
            return MethodHandles.insertArguments(copyAll, 1, template).asType(RESET);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LoggerFactory.getLogger("polydium/perf").info("ETF state reuse disabled for {}: {}", type.getName(), e.toString());
            return null;
        }
    }
}
//?}
