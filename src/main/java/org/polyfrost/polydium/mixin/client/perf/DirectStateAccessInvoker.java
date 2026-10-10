package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public interface DirectStateAccessInvoker {
    @Invoker("flushMappedBufferRange")
    void polydium$flushMappedBufferRange(int handle, long offset, long length, int usage);
}
//?}
