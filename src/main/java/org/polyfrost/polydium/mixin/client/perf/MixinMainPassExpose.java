package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.LevelRenderer;
import org.polyfrost.polydium.client.perf.MainPassSwap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LevelRenderer.class, priority = 800)
public abstract class MixinMainPassExpose {
    @ModifyVariable(method = "executeSolid", at = @At("TAIL"), argsOnly = true)
    private RenderPass polydium$exposeReopenedPass(RenderPass pass) {
        RenderPass reopened = MainPassSwap.peek();

        return reopened != null ? reopened : pass;
    }
}
//?}
