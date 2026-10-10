package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawableGizmoPrimitives.class)
public interface DrawableGizmoPrimitivesAccessor {
    @Accessor("isEmpty")
    boolean polydium$isEmpty();
}
//?}
