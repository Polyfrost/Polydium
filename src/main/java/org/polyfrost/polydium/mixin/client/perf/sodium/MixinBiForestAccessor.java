package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.tree.Tree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.tree.BaseBiForest", remap = false)
public interface MixinBiForestAccessor {
    @Accessor("mainTree")
    Tree polydium$mainTree();

    @Accessor("secondaryTree")
    Tree polydium$secondaryTree();
}
//?}
