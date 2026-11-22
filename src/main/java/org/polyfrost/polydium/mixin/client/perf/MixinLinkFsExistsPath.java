package org.polyfrost.polydium.mixin.client.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.server.packs.linkfs.LinkFSPath")
public interface MixinLinkFsExistsPath {
    @Invoker("exists")
    boolean polydium$exists();
}
