package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlBuffer.Direct.class)
public interface GlBufferDirectAccessor {
    @Accessor("dsa")
    DirectStateAccess polydium$dsa();
}
//?}
