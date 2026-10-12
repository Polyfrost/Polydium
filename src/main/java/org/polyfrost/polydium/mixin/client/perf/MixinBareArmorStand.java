package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinBareArmorStand {
    @Unique
    private static final Class<?>[] POLYDIUM_VANILLA_LAYERS = {HumanoidArmorLayer.class, ItemInHandLayer.class, WingsLayer.class, CustomHeadLayer.class};

    @Shadow
    @Final
    protected List<RenderLayer<?, ?>> layers;

    @WrapOperation(
            //? if >= 26.1 {
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            //?} else {
            /*method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            *///?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V")
    )
    private void polydium$skipBareArmorStandPose(EntityModel<?> model, Object state, Operation<Void> original) {
        if (state instanceof ArmorStandRenderState stand
                && ((Object) this).getClass() == ArmorStandRenderer.class
                && model.root().getClass() == ModelPart.class
                && polydium$isBare(stand)
                && this.polydium$hasVanillaLayers()) {
            return;
        }
        original.call(model, state);
    }

    @Unique
    private boolean polydium$hasVanillaLayers() {
        List<RenderLayer<?, ?>> layers = this.layers;
        if (layers.size() != POLYDIUM_VANILLA_LAYERS.length) {
            return false;
        }
        for (int i = 0; i < POLYDIUM_VANILLA_LAYERS.length; i++) {
            if (layers.get(i).getClass() != POLYDIUM_VANILLA_LAYERS[i]) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private static boolean polydium$isBare(ArmorStandRenderState state) {
        return state.headEquipment.isEmpty() && state.chestEquipment.isEmpty() && state.legsEquipment.isEmpty() && state.feetEquipment.isEmpty()
                //? if >= 1.21.11 {
                && state.rightHandItemState.isEmpty() && state.leftHandItemState.isEmpty()
                //?} else {
                /*&& state.rightHandItem.isEmpty() && state.leftHandItem.isEmpty()
                *///?}
                && state.headItem.isEmpty() && state.wornHeadType == null;
    }
}
//?}
