package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
//? if >= 26.1 {
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
//?} else {
/*import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
*///?}
import net.minecraft.core.Direction;
import org.joml.Vector3fc;
import org.polyfrost.polydium.client.perf.PaneCapCull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(/*? if >= 26.1 {*/UnbakedCuboidGeometry/*?} else {*//*SimpleUnbakedGeometry*//*?}*/.class)
public abstract class MixinPaneCapCull {
    @ModifyExpressionValue(
            method = "bake*",
            //? if >= 26.1 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/CuboidFace;cullForDirection()Lnet/minecraft/core/Direction;")
            //?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockElementFace;cullForDirection()Lnet/minecraft/core/Direction;")
            *///?}
    )
    private static Direction polydium$capCullFace(Direction cullFace, @Local /*? if >= 26.1 {*/CuboidModelElement/*?} else {*//*BlockElement*//*?}*/ element, @Local Direction facing) {
        if (cullFace != null || element.rotation() != null) {
            return cullFace;
        }
        Vector3fc from = element.from();
        Vector3fc to = element.to();
        return PaneCapCull.cullFace(from.x(), from.y(), from.z(), to.x(), to.y(), to.z(), facing);
    }
}
//?}
