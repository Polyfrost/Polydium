package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else if >= 1.21.10 {
/*import net.minecraft.client.renderer.RenderType;
*///?}

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class BatchOrder {
    //? if >= 1.21.10 {
    public static final Comparator<Object> BY_PIPELINE = Comparator.comparingInt(
            entry -> System.identityHashCode(((RenderType) ((Map.Entry<?, ?>) entry).getKey()).pipeline()));
    //?}

    //? if >= 26.2 {
    public static final Predicate<Object> OPAQUE =
            entry -> ((Map.Entry<?, ?>) entry).getKey() instanceof RenderType type && !type.hasBlending();
    //?}

    private BatchOrder() {
    }

    public static <T> void sortRuns(List<T> items, Predicate<? super T> sortable, Comparator<? super T> order) {
        int size = items.size();
        for (int start = 0; start < size; start++) {
            if (!sortable.test(items.get(start)))
                continue;

            int end = start + 1;
            while (end < size && sortable.test(items.get(end)))
                end++;

            if (end - start > 1)
                items.subList(start, end).sort(order);

            start = end;
        }
    }
}
