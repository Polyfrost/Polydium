package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.10 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.MutableComponent;
import org.polyfrost.polydium.client.perf.NameTagReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerScoreTextMemo {
    //? if >= 1.21.4 {
    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;append(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 2))
    //?} else {
    /^@ModifyExpressionValue(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;append(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 2))
    ^///?}
    private MutableComponent polydium$reuseScoreText(MutableComponent scoreText, @Local(argsOnly = true) AbstractClientPlayer player) {
        return ((NameTagReuse.Memo) player).polydium$reuseScoreText(scoreText) instanceof MutableComponent reused ? reused : scoreText;
    }
}
*///?}
