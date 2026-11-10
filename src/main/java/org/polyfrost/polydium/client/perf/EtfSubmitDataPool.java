package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import org.polyfrost.polydium.compat.Mods;
import org.slf4j.LoggerFactory;

public final class EtfSubmitDataPool {
    private static final int CAPACITY = 4096;
    private static final Object[] DATA = new Object[CAPACITY];
    private static final MethodHandle MOUNTED_STATE;
    private static final MethodHandle SET_BACKUP_STATE;
    private static final MethodHandle GET_DATA;
    private static int size;
    private static int next;

    static {
        MethodHandle mounted = null, setBackup = null, getData = null;
        if (Mods.loaded("entity_texture_features")) {
            try {
                ClassLoader loader = EtfSubmitDataPool.class.getClassLoader();
                String pkg = "traben.entity_texture_features.features.state.";
                Class<?> data = Class.forName(pkg + "ETFSubmitData", false, loader);
                Class<?> state = Class.forName(pkg + "ETFEntityRenderState", false, loader);
                int fields = 0;
                for (Field field : data.getDeclaredFields())
                    if (!Modifier.isStatic(field.getModifiers()))
                        fields++;
                if (fields == 2) {
                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    mounted = lookup.findStatic(Class.forName(pkg + "ETFState", false, loader), "state", MethodType.methodType(state))
                        .asType(MethodType.methodType(Object.class));
                    setBackup = lookup.findSetter(data, "backupState", state).asType(MethodType.methodType(void.class, Object.class, Object.class));
                    getData = lookup.findGetter(data, "data", Map.class).asType(MethodType.methodType(Map.class, Object.class));
                } else {
                    LoggerFactory.getLogger("polydium/perf").info("ETF submit data pool disabled: ETFSubmitData has {} fields", fields);
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                mounted = null;
                LoggerFactory.getLogger("polydium/perf").info("ETF submit data pool disabled: {}", e.toString());
            }
        }
        MOUNTED_STATE = mounted;
        SET_BACKUP_STATE = setBackup;
        GET_DATA = getData;
    }

    private EtfSubmitDataPool() {
    }

    public static Object acquire() throws Throwable {
        if (MOUNTED_STATE == null || next == size || !RenderSystem.isOnRenderThread())
            return null;
        Object data = DATA[next++];
        SET_BACKUP_STATE.invokeExact(data, (Object) MOUNTED_STATE.invokeExact());
        return data;
    }

    public static void created(Object data) {
        if (MOUNTED_STATE != null && next == size && size < CAPACITY && RenderSystem.isOnRenderThread()) {
            DATA[size++] = data;
            next = size;
        }
    }

    public static void recycle() {
        try {
            for (int i = 0; i < next; i++) {
                SET_BACKUP_STATE.invokeExact(DATA[i], (Object) null);
                ((Map<?, ?>) GET_DATA.invokeExact(DATA[i])).clear();
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        next = 0;
    }
}
//?}
