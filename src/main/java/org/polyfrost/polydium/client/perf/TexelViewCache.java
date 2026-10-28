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
            long cached = this.find(info.offset(), info.range(), info.format());
            if (cached == 0L) {
                return false;
            }
            view.put(0, cached);
            return true;
        }

        public boolean store(VkBufferViewCreateInfo info, long view) {
            return this.store(info.offset(), info.range(), info.format(), view);
        }

        public long find(long offset, long range, int format) {
            for (int i = 0; i < this.count; i++) {
                int base = i * 4;
                if (this.views[base] == offset && this.views[base + 1] == range && this.views[base + 2] == format) {
                    return this.views[base + 3];
                }
            }
            return 0L;
        }

        public boolean store(long offset, long range, int format, long view) {
            if (this.count == MAX_VIEWS) {
                return false;
            }
            int base = this.count++ * 4;
            this.views[base] = offset;
            this.views[base + 1] = range;
            this.views[base + 2] = format;
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
