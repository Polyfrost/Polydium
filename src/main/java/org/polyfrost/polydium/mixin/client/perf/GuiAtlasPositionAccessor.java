package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 && < 26.1 {
/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.render.GuiRenderer$AtlasPosition")
public interface GuiAtlasPositionAccessor {
    @Accessor("lastAnimatedOnFrame")
    void polydium$setLastAnimatedOnFrame(int frame);
}
*///?}
