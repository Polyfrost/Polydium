package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import org.polyfrost.polydium.client.perf.NameTagReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
public abstract class MixinAvatarScoreTextMemo {
    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientAvatarEntity;belowNameDisplay()Lnet/minecraft/network/chat/Component;"))
    private Component polydium$reuseScoreText(Component scoreText, @Local(argsOnly = true) Avatar avatar) {
        return ((NameTagReuse.Memo) avatar).polydium$reuseScoreText(scoreText);
    }
}
*///?}
