package org.polyfrost.polydium.mixin.client.perf.opengl;

//? if >= 26.2 && < 26.3 {
/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlStateManager$BooleanState")
public interface GlBooleanStateAccessor {
    @Accessor("enabled")
    boolean polydium$enabled();

    @Accessor("enabled")
    void polydium$setEnabled(boolean enabled);
}
*///?}
