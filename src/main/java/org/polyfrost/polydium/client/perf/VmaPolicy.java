package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaBudget;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

public final class VmaPolicy {
    public static final long BLOCK_BYTES = Long.getLong("polydium.vmaBlockMiB", 64L) << 20;
    private static final long DEDICATED_MIN_BYTES = Long.getLong("polydium.vmaDedicatedMiB", 4L) << 20;
    private static final long MAPPED_DEDICATED_MIN_BYTES = Long.getLong("polydium.vmaMappedDedicatedMiB", 8L) << 20;

    private static boolean transientScope;

    private VmaPolicy() {
    }

    public static <T> T pooled(final Supplier<T> create) {
        transientScope = true;
        try {
            return create.get();
        } finally {
            transientScope = false;
        }
    }

    public static int create(final long allocator, final long bytes, final int flags, final IntUnaryOperator create) {
        if (bytes < ((flags & Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT) != 0 ? MAPPED_DEDICATED_MIN_BYTES : DEDICATED_MIN_BYTES)
            || transientScope
            || (flags & (Vma.VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT | Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT)) != 0
            || nearAllocationLimit(allocator)) {
            return create.applyAsInt(flags);
        }
        int result = create.applyAsInt(flags | Vma.VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT);
        return result < 0 ? create.applyAsInt(flags) : result;
    }

    private static boolean nearAllocationLimit(final long allocator) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer props = stack.mallocPointer(1);
            Vma.vmaGetPhysicalDeviceProperties(allocator, props);
            long limit = Integer.toUnsignedLong(VkPhysicalDeviceProperties.create(props.get(0)).limits().maxMemoryAllocationCount());
            VmaBudget.Buffer budgets = VmaBudget.calloc(VK10.VK_MAX_MEMORY_HEAPS, stack);
            Vma.vmaGetHeapBudgets(allocator, budgets);
            long count = 0;
            for (VmaBudget budget : budgets) {
                count += Integer.toUnsignedLong(budget.statistics().blockCount());
            }
            return count > limit * 3 / 4;
        }
    }

    public static long imageBytes(final int bytesPerTexel, final int width, final int height, final int layers, final int mipLevels) {
        long base = (long) bytesPerTexel * width * height * layers;
        return mipLevels > 1 ? base + base / 3 : base;
    }
}
//?}
