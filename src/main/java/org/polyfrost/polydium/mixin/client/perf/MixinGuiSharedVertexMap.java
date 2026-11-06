package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import net.minecraft.client.gui.render.GuiRenderer;
import org.lwjgl.system.MemoryUtil;
import org.polyfrost.polydium.client.perf.MappedSliceView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(GuiRenderer.class)
public class MixinGuiSharedVertexMap {
    @Unique
    private final Map<GpuBuffer, GpuBuffer.MappedView> polydium$mappings = new IdentityHashMap<>();
    @Unique
    private final MappedSliceView polydium$slice = new MappedSliceView();

    @WrapOperation(method = "recordDraws", require = 0, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;mapBuffer(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZ)Lcom/mojang/blaze3d/buffers/GpuBuffer$MappedView;"))
    private GpuBuffer.MappedView polydium$sliceSharedMapping(CommandEncoder encoder, GpuBufferSlice slice, boolean read, boolean write, Operation<GpuBuffer.MappedView> original) {
        GpuBuffer buffer = slice.buffer();
        GpuBuffer.MappedView whole = this.polydium$mappings.get(buffer);
        if (whole == null) {
            whole = original.call(encoder, buffer.slice(), read, write);
            this.polydium$mappings.put(buffer, whole);
        }
        this.polydium$slice.data = MemoryUtil.memSlice(whole.data(), (int) slice.offset(), (int) slice.length());
        return this.polydium$slice;
    }

    @Inject(method = "recordDraws", at = @At("TAIL"))
    private void polydium$unmapShared(CallbackInfo ci) {
        if (this.polydium$mappings.isEmpty()) {
            return;
        }
        for (GpuBuffer.MappedView view : this.polydium$mappings.values()) {
            view.close();
        }
        this.polydium$mappings.clear();
        this.polydium$slice.data = null;
    }
}
*///?}
