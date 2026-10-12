package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if < 26.2 {
/*import com.mojang.renderpearl.backend.opengl.DirectStateAccess;

import java.nio.ByteBuffer;
*///?}

@Mixin(GlBuffer.class)
public interface GlBufferAccessor {
    //? if >= 26.2 {
    @Accessor("canPersistentMap")
    boolean polydium$canPersistentMap();
    //?} else {
    /*@Accessor("persistentBuffer")
    ByteBuffer polydium$persistentBuffer();

    @Accessor("dsa")
    DirectStateAccess polydium$dsa();

    @Accessor("handle")
    int polydium$handle();
    *///?}
}
//?}
