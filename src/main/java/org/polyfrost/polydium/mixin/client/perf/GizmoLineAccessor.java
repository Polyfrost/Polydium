package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
//? if < 26.2 {
/*import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.gen.Accessor;
*///?}
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives$Line")
public interface GizmoLineAccessor {
    //? if < 26.2 {
    /*@Accessor("start")
    Vec3 polydium$start();

    @Accessor("end")
    Vec3 polydium$end();

    @Accessor("color")
    int polydium$color();

    @Accessor("width")
    float polydium$width();
    *///?}
}
//?}
