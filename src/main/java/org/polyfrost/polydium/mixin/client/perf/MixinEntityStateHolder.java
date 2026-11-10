package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityStateReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class MixinEntityStateHolder implements EntityStateReuse.Holder {
    @Unique
    private EntityRenderState polydium$reusedState;
    @Unique
    private Object polydium$reusedStateOwner;
    @Unique
    private int polydium$reusedStatePass;
    @Unique
    private Object polydium$etfState;

    @Override
    public EntityRenderState polydium$reusedState() {
        return this.polydium$reusedState;
    }

    @Override
    public Object polydium$reusedStateOwner() {
        return this.polydium$reusedStateOwner;
    }

    @Override
    public int polydium$reusedStatePass() {
        return this.polydium$reusedStatePass;
    }

    @Override
    public void polydium$setReusedState(EntityRenderState state, Object owner, int pass) {
        this.polydium$reusedState = state;
        this.polydium$reusedStateOwner = owner;
        this.polydium$reusedStatePass = pass;
    }

    @Override
    public Object polydium$etfState() {
        return this.polydium$etfState;
    }

    @Override
    public void polydium$setEtfState(Object state) {
        this.polydium$etfState = state;
    }
}
//?}
