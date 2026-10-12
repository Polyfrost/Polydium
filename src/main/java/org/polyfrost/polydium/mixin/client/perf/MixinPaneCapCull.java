package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
//? if >= 26.1 {
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
//?} else if >= 1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
*///?} else if >= 1.21.4 {
/*import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.resources.model.SimpleBakedModel;
*///?} else {
/*import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
*///?}
import net.minecraft.core.Direction;
import org.joml.Vector3fc;
import org.polyfrost.polydium.client.perf.PaneCapCull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(/*? if >= 26.1 {*/UnbakedCuboidGeometry/*?} else if >= 1.21.5 {*//*SimpleUnbakedGeometry*//*?} else if >= 1.21.4 {*//*SimpleBakedModel*//*?} else {*//*BlockModel*//*?}*/.class)
public abstract class MixinPaneCapCull {
    @ModifyExpressionValue(
            //? if >= 1.21.11 {
            method = "bake*",
            //?} else if >= 1.21.5 {
            /*method = "method_67933",
            *///?} else if >= 1.21.4 {
            /*method = "bakeElements",
            *///?} else {
            /*method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;",
            *///?}
            //? if >= 26.1 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/CuboidFace;cullForDirection()Lnet/minecraft/core/Direction;")
            //?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockElementFace;cullForDirection()Lnet/minecraft/core/Direction;")
            *///?}
    )
    private /*? if >= 1.21.4 {*/static /*?}*/Direction polydium$capCullFace(Direction cullFace, @Local /*? if >= 26.1 {*/CuboidModelElement/*?} else {*//*BlockElement*//*?}*/ element, @Local Direction facing) {
        //? if >= 1.21.5 {
        if (cullFace != null || element.rotation() != null) {
            return cullFace;
        }
        Vector3fc from = element.from();
        Vector3fc to = element.to();
        //?} else {
        /*if (cullFace != null || element.rotation != null) {
            return cullFace;
        }
        Vector3fc from = element.from;
        Vector3fc to = element.to;
        *///?}
        return PaneCapCull.cullFace(from.x(), from.y(), from.z(), to.x(), to.y(), to.z(), facing);
    }
}
//?}
