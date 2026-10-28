package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(XoroshiroRandomSource.XoroshiroPositionalRandomFactory.class)
public interface XoroshiroPositionalFactoryAccessor {
    @Accessor("seedLo")
    long polydium$seedLo();

    @Accessor("seedHi")
    long polydium$seedHi();
}
