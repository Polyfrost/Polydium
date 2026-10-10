package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.EntityCheckOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer", remap = false)
public abstract class MixinEntityCheckOrderSodium implements EntityCheckOrder {
    @Unique
    private Entity polydium$deferred;
    @Unique
    private Entity polydium$asked;
    @Unique
    private boolean polydium$testing;

    @Shadow
    //? if >= 26.3 {
    public abstract <T extends Entity, S extends EntityRenderState> boolean isEntityVisible(EntityRenderer<T, S> renderer, T entity, float partialTicks);
    //?} else {
    /*public abstract <T extends Entity, S extends EntityRenderState> boolean isEntityVisible(EntityRenderer<T, S> renderer, T entity);
    *///?}

    @ModifyExpressionValue(
            method = "isEntityVisible",
            at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/SodiumWorldRenderer;useEntityCulling:Z")
    )
    private boolean polydium$deferSectionTest(boolean useEntityCulling, @Local(argsOnly = true) Entity entity) {
        if (this.polydium$testing) {
            return useEntityCulling;
        }
        if (!useEntityCulling) {
            this.polydium$asked = entity;
            return useEntityCulling;
        }
        this.polydium$deferred = entity;
        return false;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean polydium$finishEntityCheck(boolean vanillaResult, EntityRenderer<?, ?> renderer, Entity entity, float partialTicks) {
        if (this.polydium$asked == entity) {
            this.polydium$asked = null;
            return vanillaResult;
        }
        this.polydium$deferred = null;
        if (!vanillaResult) {
            return false;
        }
        this.polydium$testing = true;
        try {
            //? if >= 26.3 {
            return this.isEntityVisible((EntityRenderer) renderer, entity, partialTicks);
            //?} else {
            /*return this.isEntityVisible((EntityRenderer) renderer, entity);
            *///?}
        } finally {
            this.polydium$testing = false;
        }
    }
}
//?}
