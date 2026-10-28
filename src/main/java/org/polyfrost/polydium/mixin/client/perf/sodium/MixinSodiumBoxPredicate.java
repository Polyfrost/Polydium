package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinSodiumBoxPredicate {
    @Shadow
    private SectionTree renderTree;

    @Unique
    private final SectionTree.NotInTreePredicate polydium$isSectionEmpty = (x, y, z) -> this.isSectionEmpty(x, y, z);

    @Shadow
    private boolean isSectionEmpty(int x, int y, int z) {
        throw new AssertionError();
    }

    @Overwrite
    public boolean isBoxVisible(double x1, double y1, double z1, double x2, double y2, double z2) {
        return this.renderTree == null || this.renderTree.isBoxVisible(x1, y1, z1, x2, y2, z2, this.polydium$isSectionEmpty);
    }
}
//?}
