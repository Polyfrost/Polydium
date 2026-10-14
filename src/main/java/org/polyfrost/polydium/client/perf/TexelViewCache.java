package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

public interface TexelViewCache {
    Views polydium$texelViews();

    final class Views {
        private static final int MAX_VIEWS = 8;

        private final long[] views = new long[MAX_VIEWS * 4];
        private int count;

        public boolean reuse(VkBufferViewCreateInfo info, LongBuffer view) {
            for (int i = 0; i < this.count; i++) {
                int base = i * 4;
                if (this.views[base] == info.offset() && this.views[base + 1] == info.range() && this.views[base + 2] == info.format()) {
                    view.put(0, this.views[base + 3]);
                    return true;
                }
            }
            return false;
        }

        public boolean store(VkBufferViewCreateInfo info, long view) {
            if (this.count == MAX_VIEWS) {
                return false;
            }
            int base = this.count++ * 4;
            this.views[base] = info.offset();
            this.views[base + 1] = info.range();
            this.views[base + 2] = info.format();
            this.views[base + 3] = view;
            return true;
        }

        public void destroy(VkDevice device) {
            for (int i = 0; i < this.count; i++) {
                VK10.vkDestroyBufferView(device, this.views[i * 4 + 3], null);
            }
            this.count = 0;
        }
    }
}
//?}
