package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.Particle;
//? if < 26.2
/*import net.minecraft.world.entity.Entity;*/
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.polyfrost.polydium.client.perf.ParticleAirCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Particle.class)
public abstract class MixinParticleAirCollision {
    @WrapOperation(
            method = "move(DDD)V",
            at = @At(
                    value = "INVOKE",
                    //? if >= 26.2 {
                    target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/phys/shapes/CollisionContext;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"
                    //?} else {
                    /*target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"
                    *///?}
            )
    )
    private Vec3 polydium$skipAirCollision(/*? if >= 26.2 {*/CollisionContext/*?} else {*//*Entity*//*?}*/ context, Vec3 movement, AABB box, Level level, List<VoxelShape> colliders, Operation<Vec3> original) {
        if (colliders.isEmpty()
            //? if >= 26.2 {
            && ParticleAirCollision.cannotCollide(level, context, box, movement)
            //?} else {
            /*&& context == null && ParticleAirCollision.cannotCollide(level, CollisionContext.empty(), box, movement)
            *///?}
        ) {
            return movement;
        }
        return original.call(context, movement, box, level, colliders);
    }
}
//?}
