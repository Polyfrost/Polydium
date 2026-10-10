package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.lists.FallbackVisibleChunkCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.CullType;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.polyfrost.polydium.client.perf.TreeArrayPool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager", remap = false)
public abstract class MixinTreePoolRelease {
    @Unique
    private static final CullType[] polydium$CULL_TYPES = CullType.values();

    @Shadow @Final
    private Map<CullType, SectionTree> cullResults;
    @Shadow
    private SectionTree renderTree;

    @Unique
    private final SectionTree[] polydium$lastLive = new SectionTree[polydium$CULL_TYPES.length + 1];

    @Inject(method = "finalizeRenderLists", at = @At("RETURN"))
    private void polydium$releaseDeadTrees(CallbackInfo ci) {
        SectionTree[] last = this.polydium$lastLive;
        if (IrisShaders.packInUse()) {
            Arrays.fill(last, null);
            return;
        }
        int types = polydium$CULL_TYPES.length;
        for (int i = 0; i <= types; i++) {
            SectionTree old = last[i];
            if (old == null || this.polydium$isLive(old)) {
                continue;
            }
            for (int j = i; j <= types; j++) {
                if (last[j] == old) {
                    last[j] = null;
                }
            }
            if (!(old instanceof FallbackVisibleChunkCollector)) {
                TreeArrayPool.release(old);
            }
        }
        for (int i = 0; i < types; i++) {
            last[i] = this.cullResults.get(polydium$CULL_TYPES[i]);
        }
        last[types] = this.renderTree;
    }

    @Unique
    private boolean polydium$isLive(SectionTree tree) {
        return tree == this.renderTree || this.cullResults.containsValue(tree);
    }
}
//?}
