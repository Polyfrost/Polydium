package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

public final class PushConstantCall {
    private static final MethodHandle CALL = link();
    private static volatile boolean failed;
    private static MemorySegment function;

    private PushConstantCall() {
    }

    private static MethodHandle link() {
        try {
            if (ValueLayout.ADDRESS.byteSize() != Long.BYTES) {
                return null;
            }
            return Linker.nativeLinker().downcallHandle(FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static boolean push(VkCommandBuffer commandBuffer, long layout, int stageFlags, int offset, ByteBuffer values) {
        if (CALL == null || failed) {
            return false;
        }
        long address = commandBuffer.getCapabilities().vkCmdPushConstants;
        if (address == 0L) {
            return false;
        }
        MemorySegment target = function;
        if (target == null || target.address() != address) {
            function = target = MemorySegment.ofAddress(address);
        }
        try {
            CALL.invokeExact(target, commandBuffer.address(), layout, stageFlags, offset, values.remaining(), MemoryUtil.memAddress(values));
            return true;
        } catch (Throwable throwable) {
            failed = true;
            return false;
        }
    }
}
//?}
