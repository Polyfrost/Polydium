package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.NameTagReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRendererNameTagMemo {
    //? if >= 26.2 {
    @ModifyExpressionValue(method = "extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getNameTag(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;"))
    //?} else if >= 1.21.4 {
    /*@ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getNameTag(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;"))
    *///?} else {
    /*@ModifyExpressionValue(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    *///?}
    private Component polydium$reuseNameTag(Component nameTag, @Local(argsOnly = true) Entity entity) {
        return ((NameTagReuse.Memo) entity).polydium$reuseNameTag(nameTag);
    }

    //? if >= 26.2 {
    @ModifyExpressionValue(method = "extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;belowNameDisplay()Lnet/minecraft/network/chat/Component;"))
    //?} else if >= 26.1 {
    /*@ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;belowNameDisplay()Lnet/minecraft/network/chat/Component;"))
    *///?}
    //? if >= 26.1 {
    private Component polydium$reuseScoreText(Component scoreText, @Local(argsOnly = true) Entity entity) {
        return ((NameTagReuse.Memo) entity).polydium$reuseScoreText(scoreText);
    }
    //?}
}
