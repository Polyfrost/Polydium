package org.polyfrost.polydium.client.perf;

//? if >= 26.3 {
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;

public interface DensityPoolContexts {
    void polydium$track(SamplerContext context);

    void polydium$closeContexts();

    static void closeCaches(SamplerContext context) {
        Object[] cells = (Object[]) Handles.CELLS.get(context);
        if (cells == null) {
            return;
        }
        for (Object cell : cells) {
            ScopedDensityBuffer buffer = (ScopedDensityBuffer) Handles.BUFFER.get(cell);
            if (buffer != null) {
                Handles.BUFFER.set(cell, null);
                Handles.VOLUME.set(cell, null);
                buffer.close();
            }
        }
    }

    final class Handles {
        static final VarHandle CELLS, BUFFER, VOLUME;

        static {
            try {
                MethodHandles.Lookup context = MethodHandles.privateLookupIn(SamplerContext.class, MethodHandles.lookup());
                Class<?> cellClass = Class.forName(SamplerContext.class.getName() + "$CacheCell");
                MethodHandles.Lookup cell = MethodHandles.privateLookupIn(cellClass, MethodHandles.lookup());
                CELLS = context.findVarHandle(SamplerContext.class, "cacheCells", cellClass.arrayType());
                BUFFER = cell.findVarHandle(cellClass, "buffer", ScopedDensityBuffer.class);
                VOLUME = cell.findVarHandle(cellClass, "volume", Class.forName("net.minecraft.world.level.levelgen.densityfunction.DensityVolume"));
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private Handles() {
        }
    }
}
//?}
