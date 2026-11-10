package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiItemAtlasReuse {
    @Shadow
    @Final
    private Map<Object, Object> atlasPositions;
    @Shadow
    private GpuTexture itemsAtlas;
    @Shadow
    private int itemAtlasX;
    @Shadow
    private int itemAtlasY;
    @Shadow
    private int frameNumber;
    @Shadow
    @Final
    GuiRenderState renderState;

    @Unique
    private final ArrayDeque<Object> polydium$freeSlots = new ArrayDeque<>();
    @Unique
    private Object polydium$reusedSlot;

    @Inject(method = "calculateAtlasSizeInPixels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;invalidateItemAtlas()V", ordinal = 0), cancellable = true)
    private void polydium$reclaimSlots(int slotSize, CallbackInfoReturnable<Integer> cir) {
        Set<Object> frame = this.renderState.getItemModelIdentities();
        int atlasSize = this.itemsAtlas.getWidth(0);
        int perRow = atlasSize / slotSize;
        int capacity = perRow * perRow;
        if (frame.size() > capacity) {
            return;
        }

        int needed = this.atlasPositions.size();
        for (Object key : frame) {
            if (!this.atlasPositions.containsKey(key)) {
                needed++;
            }
        }

        int evict = needed - capacity;
        Iterator<Map.Entry<Object, Object>> entries = this.atlasPositions.entrySet().iterator();
        while (evict > 0 && entries.hasNext()) {
            Map.Entry<Object, Object> entry = entries.next();
            if (!frame.contains(entry.getKey())) {
                this.polydium$freeSlots.add(entry.getValue());
                entries.remove();
                evict--;
            }
        }
        cir.setReturnValue(atlasSize);
    }

    @Inject(method = "invalidateItemAtlas", at = @At("HEAD"))
    private void polydium$dropFreeSlots(CallbackInfo ci) {
        this.polydium$freeSlots.clear();
        this.polydium$reusedSlot = null;
    }

    @ModifyExpressionValue(method = "method_71055", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object polydium$takeFreeSlot(Object position, @Local(argsOnly = true, ordinal = 0) int slotSize, @Local(argsOnly = true, ordinal = 1) int atlasSize,
                                         @Local(argsOnly = true) GuiItemRenderState item) {
        this.polydium$reusedSlot = null;
        if (position != null || this.polydium$freeSlots.isEmpty()) {
            return position;
        }

        int rowEnd = this.itemAtlasX + slotSize > atlasSize ? this.itemAtlasY + 2 * slotSize : this.itemAtlasY + slotSize;
        if (rowEnd <= atlasSize) {
            return position;
        }

        Object slot = this.polydium$freeSlots.poll();
        ((GuiAtlasPositionAccessor) slot).polydium$setLastAnimatedOnFrame(this.frameNumber - 1);
        this.atlasPositions.put(item.itemStackRenderState().getModelIdentity(), slot);
        this.polydium$reusedSlot = slot;
        return slot;
    }

    @WrapOperation(method = "method_71055", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/TrackingItemStackRenderState;isAnimated()Z"))
    private boolean polydium$redrawReusedSlot(TrackingItemStackRenderState state, Operation<Boolean> original) {
        return this.polydium$reusedSlot != null || original.call(state);
    }
}
*///?}
