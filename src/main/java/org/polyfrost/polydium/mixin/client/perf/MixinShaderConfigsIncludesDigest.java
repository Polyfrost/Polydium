package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.ShaderManager;
import org.polyfrost.polydium.client.perf.ShaderIncludesDigest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShaderManager.Configs.class)
public abstract class MixinShaderConfigsIncludesDigest implements ShaderIncludesDigest {
    @Unique
    private volatile byte[] polydium$includesDigest;

    @Override
    public byte[] polydium$includesDigest() {
        return this.polydium$includesDigest;
    }

    @Override
    public void polydium$setIncludesDigest(byte[] digest) {
        this.polydium$includesDigest = digest;
    }
}
//?}
