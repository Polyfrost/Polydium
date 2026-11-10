package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
//? if >= 26.2 && < 26.3 {
/*import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
*///?} else if < 26.2 {
/*import org.polyfrost.polydium.vulkan.VulkanBindGroupLayout;
*///?}
import it.unimi.dsi.fastutil.objects.ReferenceList;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class VulkanDynamicState {
    private static long commandBuffer;
    private static boolean viewportSet;
    private static float viewportX, viewportY, viewportWidth, viewportHeight, viewportMinDepth, viewportMaxDepth;
    private static boolean scissorSet;
    private static int scissorX, scissorY, scissorWidth, scissorHeight;
    private static long pipeline;
    private static long descriptorLayout;
    private static int descriptorCount = -1;
    private static Object[] descriptors = new Object[16];
    private static boolean sameDescriptors;
    public static final Object[] NO_SLOTS = {};
    private static final ConcurrentHashMap<String, Integer> SLOTS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_SLOT = new AtomicInteger();

    private VulkanDynamicState() {
    }

    public static void reset() {
        commandBuffer = 0L;
        viewportSet = false;
        scissorSet = false;
        pipeline = 0L;
        forgetDescriptors();
    }

    public static void forgetPipeline() {
        pipeline = 0L;
    }

    public static boolean changesPipeline(long buffer, long boundPipeline) {
        if (buffer == commandBuffer && boundPipeline == pipeline) {
            return false;
        }
        track(buffer);
        pipeline = boundPipeline;
        return true;
    }

    public static void forgetDescriptors() {
        if (descriptorCount > 0) {
            Arrays.fill(descriptors, 0, descriptorCount, null);
        }
        descriptorLayout = 0L;
        descriptorCount = -1;
    }

    public static boolean changesDescriptors(long buffer, long layout, ReferenceList<Object> values) {
        int count = values.size();
        beginDescriptors(buffer, layout, count);
        for (int index = 0; index < count; index++) {
            if (!descriptor(index, count, values.get(index))) {
                return true;
            }
        }
        return endDescriptors(layout, count);
    }

    public static boolean changesViewport(long buffer, float x, float y, float width, float height, float minDepth, float maxDepth) {
        if (buffer == commandBuffer && viewportSet && x == viewportX && y == viewportY && width == viewportWidth
            && height == viewportHeight && minDepth == viewportMinDepth && maxDepth == viewportMaxDepth) {
            return false;
        }
        track(buffer);
        viewportSet = true;
        viewportX = x;
        viewportY = y;
        viewportWidth = width;
        viewportHeight = height;
        viewportMinDepth = minDepth;
        viewportMaxDepth = maxDepth;
        return true;
    }

    public static boolean changesScissor(long buffer, int x, int y, int width, int height) {
        if (buffer == commandBuffer && scissorSet && x == scissorX && y == scissorY && width == scissorWidth && height == scissorHeight) {
            return false;
        }
        track(buffer);
        scissorSet = true;
        scissorX = x;
        scissorY = y;
        scissorWidth = width;
        scissorHeight = height;
        return true;
    }

    public static boolean changesViewport(VkCommandBuffer buffer, int firstViewport, VkViewport.Buffer viewports) {
        if (firstViewport != 0 || viewports.remaining() != 1) {
            reset();
            return true;
        }
        long viewport = viewports.address();
        return changesViewport(buffer.address(), VkViewport.nx(viewport), VkViewport.ny(viewport), VkViewport.nwidth(viewport),
            VkViewport.nheight(viewport), VkViewport.nminDepth(viewport), VkViewport.nmaxDepth(viewport));
    }

    public static boolean changesScissor(VkCommandBuffer buffer, int firstScissor, VkRect2D.Buffer scissors) {
        if (firstScissor != 0 || scissors.remaining() != 1) {
            reset();
            return true;
        }
        long scissor = scissors.address();
        return changesScissor(buffer.address(), VkOffset2D.nx(scissor + VkRect2D.OFFSET), VkOffset2D.ny(scissor + VkRect2D.OFFSET),
            VkExtent2D.nwidth(scissor + VkRect2D.EXTENT), VkExtent2D.nheight(scissor + VkRect2D.EXTENT));
    }

    public static int slot(String name) {
        Integer slot = SLOTS.get(name);
        return slot != null ? slot : SLOTS.computeIfAbsent(name, key -> NEXT_SLOT.getAndIncrement());
    }

    public static Object[] setSlot(Object[] slots, String name, Object value) {
        int slot = slot(name);
        if (slot >= slots.length) {
            if (value == null) {
                return slots;
            }
            slots = Arrays.copyOf(slots, slot + 8);
        }
        slots[slot] = value;
        return slots;
    }

    //? if < 26.3 {
    /*public static int[] slots(List<VulkanBindGroupLayout.Entry> entries) {
        int[] slots = new int[entries.size()];
        for (int index = 0; index < slots.length; index++) {
            slots[index] = slot(entries.get(index).name());
        }
        return slots;
    }

    public static boolean changesDescriptors(long buffer, long layout, List<VulkanBindGroupLayout.Entry> entries, int[] slots, Object[] textureSlots, Object[] uniformSlots) {
        int count = entries.size();
        beginDescriptors(buffer, layout, count);
        for (int index = 0; index < count; index++) {
            Object[] values = entries.get(index).type() == VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE ? textureSlots : uniformSlots;
            int slot = slots[index];
            if (!descriptor(index, count, slot < values.length ? values[slot] : null)) {
                return true;
            }
        }
        return endDescriptors(layout, count);
    }
    *///?}

    //? if >= 26.2 && < 26.3 {
    /*public interface LayoutSlots {
        int[] polydium$slots();
    }
    *///?}

    private static void beginDescriptors(long buffer, long layout, int count) {
        sameDescriptors = buffer == commandBuffer && layout == descriptorLayout && count == descriptorCount;
        if (!sameDescriptors) {
            track(buffer);
            forgetDescriptors();
            if (descriptors.length < count) {
                descriptors = new Object[count];
            }
        }
    }

    private static boolean descriptor(int index, int count, Object value) {
        if (value == null) {
            Arrays.fill(descriptors, 0, count, null);
            descriptorLayout = 0L;
            descriptorCount = -1;
            return false;
        }
        if (!sameDescriptors || !descriptors[index].equals(value)) {
            sameDescriptors = false;
            descriptors[index] = value;
        }
        return true;
    }

    private static boolean endDescriptors(long layout, int count) {
        if (sameDescriptors) {
            return false;
        }
        descriptorLayout = layout;
        descriptorCount = count;
        return true;
    }

    public static final class Bindings {
        private long vertexBuffer;
        private long vertexOffset;
        private long indexBuffer;
        private long indexOffset;
        private int indexType;

        public boolean changesVertexBuffer(int slot, long buffer, long offset) {
            if (slot != 0) {
                return true;
            }
            if (buffer == this.vertexBuffer && offset == this.vertexOffset) {
                return false;
            }
            this.vertexBuffer = buffer;
            this.vertexOffset = offset;
            return true;
        }

        public boolean changesIndexBuffer(long buffer, long offset, int type) {
            if (buffer == this.indexBuffer && offset == this.indexOffset && type == this.indexType) {
                return false;
            }
            this.indexBuffer = buffer;
            this.indexOffset = offset;
            this.indexType = type;
            return true;
        }
    }

    private static void track(long buffer) {
        if (buffer != commandBuffer) {
            commandBuffer = buffer;
            viewportSet = false;
            scissorSet = false;
            pipeline = 0L;
            forgetDescriptors();
        }
    }
}
//?}
