package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.4 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.objectweb.asm.Opcodes;
import org.polyfrost.polydium.client.perf.UntouchedItemLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStackRenderState.class)
public abstract class MixinItemLayersOnDemand {
    @Unique
    private static final ItemStackRenderState.LayerRenderState[] POLYDIUM_NO_LAYERS = new ItemStackRenderState.LayerRenderState[0];

    @Shadow
    private ItemStackRenderState.LayerRenderState[] layers;

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/item/ItemStackRenderState;)Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;"))
    private ItemStackRenderState.LayerRenderState polydium$deferFirstLayer(ItemStackRenderState owner, Operation<ItemStackRenderState.LayerRenderState> original) {
        return null;
    }

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;layers:[Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;", opcode = Opcodes.PUTFIELD))
    private void polydium$shareNoLayers(ItemStackRenderState owner, ItemStackRenderState.LayerRenderState[] deferred, Operation<Void> original) {
        if (deferred.length == 1 && deferred[0] == null) {
            this.layers = POLYDIUM_NO_LAYERS;
        } else {
            original.call(owner, deferred);
        }
    }

    @ModifyExpressionValue(method = "firstLayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;layers:[Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;", opcode = Opcodes.GETFIELD))
    private ItemStackRenderState.LayerRenderState[] polydium$untouchedFirstLayer(ItemStackRenderState.LayerRenderState[] layers) {
        return layers.length == 0 ? UntouchedItemLayer.LAYERS : layers;
    }
}
//?}
