package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.commands.RenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPass.class)
public abstract class MixinScissorRenderAreaY {

    @Shadow @Final
    private RenderPass.RenderArea renderArea;

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass$RenderArea;height()I"))
    private int polydium$offsetByAreaY(int height) {
        return this.renderArea.y() + height;
    }
}
*///?}
