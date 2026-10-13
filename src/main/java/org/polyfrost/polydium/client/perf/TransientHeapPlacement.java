package org.polyfrost.polydium.client.perf;

//? if >= 26.2 && < 26.3 {
/*import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkMemoryType;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import java.util.Collections;
import java.util.Set;

public final class TransientHeapPlacement {

    public static final Set<Object> SUBOPTIMAL = Collections.synchronizedSet(new ReferenceOpenHashSet<>());

    private static final int HOST_VISIBLE_COHERENT = VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

    private final int[] typeToHeap = new int[VK10.VK_MAX_MEMORY_TYPES];

    private final int cpuHeap;

    private final int gpuHeap;

    public TransientHeapPlacement(VkPhysicalDevice physicalDevice, boolean deviceMemoryForStaging) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceMemoryProperties properties = VkPhysicalDeviceMemoryProperties.calloc(stack);
            VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, properties);

            int  gpu     = -1;
            long gpuSize = -1L;
            for (int i = 0; i < properties.memoryHeapCount(); i++) {
                if ((properties.memoryHeaps(i).flags() & VK10.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT) != 0 && properties.memoryHeaps(i).size() >= gpuSize) {
                    gpu     = i;
                    gpuSize = properties.memoryHeaps(i).size();
                }
            }

            int cpu = deviceMemoryForStaging ? gpu : -1;
            for (int i = 0; i < properties.memoryTypeCount(); i++) {
                VkMemoryType type = properties.memoryTypes(i);
                this.typeToHeap[i] = type.heapIndex();

                if (cpu == -1 && (type.propertyFlags() & HOST_VISIBLE_COHERENT) == HOST_VISIBLE_COHERENT
                        && (properties.memoryHeaps(type.heapIndex()).flags() & VK10.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT) == 0) {
                    cpu = type.heapIndex();
                }
            }

            this.cpuHeap = cpu;
            this.gpuHeap = gpu;
        }
    }

    public boolean suboptimal(int memoryType, boolean hostStaging) {
        int expected = hostStaging ? this.cpuHeap : this.gpuHeap;
        return expected != -1 && this.typeToHeap[memoryType] != expected;
    }
}
*///?}
