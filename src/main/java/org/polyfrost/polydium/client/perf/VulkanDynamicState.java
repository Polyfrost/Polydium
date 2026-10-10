package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import it.unimi.dsi.fastutil.objects.ReferenceList;

import java.util.Arrays;

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
        if (buffer == commandBuffer && layout == descriptorLayout && count == descriptorCount) {
            int index = 0;
            while (index < count && descriptors[index].equals(values.get(index))) {
                index++;
            }
            if (index == count) {
                return false;
            }
        }
        track(buffer);
        forgetDescriptors();
        if (descriptors.length < count) {
            descriptors = new Object[count];
        }
        for (int index = 0; index < count; index++) {
            Object value = values.get(index);
            if (value == null) {
                Arrays.fill(descriptors, 0, index, null);
                return true;
            }
            descriptors[index] = value;
        }
        descriptorLayout = layout;
        descriptorCount = count;
        return true;
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
