package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.Entity;

public final class EntityStateReuse {
    private static boolean active;
    private static int pass;

    private EntityStateReuse() {
    }

    public interface Holder {
        EntityRenderState polydium$reusedState();

        Object polydium$reusedStateOwner();

        int polydium$reusedStatePass();

        void polydium$setReusedState(EntityRenderState state, Object owner, int pass);

        Object polydium$etfState();

        void polydium$setEtfState(Object state);
    }

    public static void begin() {
        if (RenderSystem.isOnRenderThread()) {
            active = true;
            pass++;
            EtfStateReuse.install();
        }
    }

    public static void end() {
        active = false;
        EtfStateReuse.clear();
    }

    public static EntityRenderState create(EntityRenderer<?, ?> renderer, Entity entity) {
        if (!active || !RenderSystem.isOnRenderThread()) {
            return renderer.createRenderState();
        }
        Holder holder = (Holder) entity;
        EntityRenderState state = holder.polydium$reusedState();
        if (state != null && holder.polydium$reusedStateOwner() == renderer && holder.polydium$reusedStatePass() != pass) {
            holder.polydium$setReusedState(state, renderer, pass);
            ((FabricRenderState) state).clearExtraData();
            EtfStateReuse.expect(entity, true);
            return state;
        }
        state = renderer.createRenderState();
        Class<?> cls = state.getClass();
        if (cls == SlimeRenderState.class || cls == SheepRenderState.class || cls == VillagerRenderState.class) {
            holder.polydium$setReusedState(state, renderer, pass);
            EtfStateReuse.expect(entity, false);
        }
        return state;
    }
}
//?}
