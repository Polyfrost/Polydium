package org.polyfrost.polydium.mixin.client.perf;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinPlayerPassengerCount {
    @Shadow
    private ImmutableList<Entity> passengers;

    /**
     * @author Wyvest
     * @reason Stop calling for every visible entity each frame even when ticking is frozen
     */
    @SuppressWarnings("checkstyle:NoOverwrite") // TODO: needed?
    @Overwrite
    public int countPlayerPassengers() {
        ImmutableList<Entity> passengers = this.passengers;
        int count = 0;
        for (int i = 0, n = passengers.size(); i < n; i++) {
            Entity passenger = passengers.get(i);
            if (passenger instanceof Player) count++;
            count += passenger.countPlayerPassengers();
        }
        return count;
    }
}
