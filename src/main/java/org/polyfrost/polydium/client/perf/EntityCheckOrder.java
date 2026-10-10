package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public interface EntityCheckOrder {
    boolean polydium$finishEntityCheck(boolean vanillaResult, EntityRenderer<?, ?> renderer, Entity entity, float partialTicks);
}
//?}
