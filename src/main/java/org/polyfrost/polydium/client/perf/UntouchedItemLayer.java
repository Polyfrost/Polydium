package org.polyfrost.polydium.client.perf;

//? if >= 1.21.4 {
import net.minecraft.client.renderer.item.ItemStackRenderState;

public final class UntouchedItemLayer {
    public static final ItemStackRenderState.LayerRenderState[] LAYERS = {new ItemStackRenderState().new LayerRenderState()};

    private UntouchedItemLayer() {
    }
}
//?}
