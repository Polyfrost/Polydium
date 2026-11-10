package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import java.util.ArrayList;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferPool;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.polyfrost.polydium.client.perf.DensityPoolContexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DensityBufferPool.class)
public abstract class MixinDensityPoolContexts implements DensityPoolContexts {
    @Unique
    private final ArrayList<SamplerContext> polydium$contexts = new ArrayList<>();

    @Override
    public void polydium$track(SamplerContext context) {
        this.polydium$contexts.add(context);
    }

    @Override
    public void polydium$closeContexts() {
        for (int i = 0, n = this.polydium$contexts.size(); i < n; i++) {
            DensityPoolContexts.closeCaches(this.polydium$contexts.get(i));
        }
        this.polydium$contexts.clear();
    }
}
//?}
