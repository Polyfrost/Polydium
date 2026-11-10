package org.polyfrost.polydium.mixin.client.perf.cirrus;

//? if >= 1.21.10 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.renderer.SkyRenderer;
import org.polyfrost.polydium.client.perf.SkyFarPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.3 {
import com.mojang.renderpearl.api.commands.RenderPass;
//?} else {
/*import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.GpuTexture;
*///?}

@Mixin(value = SkyRenderer.class, priority = 1500)
public class MixinCirrusSkyRenderer {
    @Unique
    private static final String POLYDIUM$CIRRUS_SKY_MIXIN = "com.jvn.cirrus.mixin.SkyRendererMixin";

    //? if >= 26.3 {
    @TargetHandler(mixin = POLYDIUM$CIRRUS_SKY_MIXIN, name = "cirrus$clearSunMask")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/jvn/cirrus/client/CirrusShaders;clearSunMask(Lcom/mojang/renderpearl/api/commands/RenderPass;)V"), require = 0, expect = 0)
    private boolean polydium$keepCirrusDepth(RenderPass pass) {
        return !SkyFarPlane.isDrawing();
    }
    //?} else {
    /*@TargetHandler(mixin = POLYDIUM$CIRRUS_SKY_MIXIN, name = "cirrus$clearSunMask")
    @WrapWithCondition(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;clearDepthTexture(Lcom/mojang/renderpearl/api/textures/GpuTexture;D)V"), require = 0, expect = 0)
    private boolean polydium$keepCirrusDepth(CommandEncoder encoder, GpuTexture texture, double depth) {
        return !SkyFarPlane.isDrawing();
    }
    *///?}
}
//?}
