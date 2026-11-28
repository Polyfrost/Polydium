package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import org.polyfrost.polydium.client.perf.GuiItemBake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//~ if >= 26.1 'GuiRenderer$AtlasPosition' -> 'DynamicAtlasAllocator$Slot'
@Mixin(targets = "net.minecraft.client.gui.render.DynamicAtlasAllocator$Slot")
public class MixinGuiItemBakeSlot implements GuiItemBake.Slot {
    @Unique
    private long polydium$stamp;

    @Override
    public long polydium$stamp() {
        return this.polydium$stamp;
    }

    @Override
    public void polydium$setStamp(long stamp) {
        this.polydium$stamp = stamp;
    }
}
//?}
