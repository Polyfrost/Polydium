package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
//? if >= 26.3
import net.minecraft.client.renderer.oit.OitStage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(FeatureRenderDispatcher.PreparedFrame.class)
public class MixinEmptyFeaturePhaseFrame {
    @Shadow
    @Final
    private Map<FeatureRenderPhase<?>, List<?>> groupsByPhase;

    //? if >= 26.3 {
    @WrapMethod(method = "executePhase(Lnet/minecraft/client/renderer/feature/phase/FeatureRenderPhase;Lnet/minecraft/client/renderer/feature/FeatureFrameContext;Lnet/minecraft/client/renderer/oit/OitStage;Lcom/mojang/renderpearl/api/commands/RenderPass;)V")
    private void polydium$skipPhaseWithoutGroups(FeatureRenderPhase<?> phase, FeatureFrameContext context, OitStage stage, RenderPass renderPass, Operation<Void> original) {
        if (!this.groupsByPhase.containsKey(phase)) {
            return;
        }
        original.call(phase, context, stage, renderPass);
    }
    //?} else {
    /*@WrapMethod(method = "executePhase")
    private void polydium$skipPhaseWithoutGroups(FeatureRenderPhase<?> phase, FeatureFrameContext context, Operation<Void> original) {
        if (!this.groupsByPhase.containsKey(phase)) {
            return;
        }
        original.call(phase, context);
    }
    *///?}
}
//?}
