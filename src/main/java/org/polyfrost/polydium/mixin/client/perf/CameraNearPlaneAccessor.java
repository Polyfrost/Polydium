package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.NearPlane.class)
public interface CameraNearPlaneAccessor {
    @Accessor("forward")
    Vec3 polydium$forward();

    @Accessor("left")
    Vec3 polydium$left();

    @Accessor("up")
    Vec3 polydium$up();
}
