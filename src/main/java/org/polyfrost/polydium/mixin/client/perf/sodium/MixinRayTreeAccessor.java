package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.tree.Forest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.occlusion.RayOcclusionSectionTree", remap = false)
public interface MixinRayTreeAccessor {
    @Accessor("portalTree")
    Forest<?> polydium$portalTree();
}
//?}
