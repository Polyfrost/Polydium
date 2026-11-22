package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class MixinEntityNameStripMemo {
    @Unique
    private Component polydium$strippedNameSource;

    @Unique
    private Component polydium$strippedName;

    @WrapOperation(method = "getName()Lnet/minecraft/network/chat/Component;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removeAction(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;"))
    private Component polydium$memoStrippedName(Component customName, Operation<Component> original) {
        if (!RenderSystem.isOnRenderThread()) {
            return original.call(customName);
        }
        if (customName != null && customName == this.polydium$strippedNameSource) {
            return this.polydium$strippedName;
        }
        Component stripped = original.call(customName);
        this.polydium$strippedName = stripped;
        this.polydium$strippedNameSource = customName;
        return stripped;
    }
}
