package org.polyfrost.polydium.mixin.client.perf.scalablelux;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ClientPacketListener.class, priority = 1100)
public abstract class MixinChunkLoadDirtyOnce {
}
