package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 && < 26.1 {
/*import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemTintLayerAccessor {
    @Accessor("tintLayers")
    int[] polydium$tintLayers();
}
*///?}
