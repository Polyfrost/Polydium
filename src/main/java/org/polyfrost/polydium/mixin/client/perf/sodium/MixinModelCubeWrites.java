package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
//? if < 1.21.4
/*import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;*/
import net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer;
import net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.polyfrost.polydium.client.perf.SodiumModelParts;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class MixinModelCubeWrites {
    @Shadow
    @Final
    private Map<String, ModelPart> children;

    @ModifyExpressionValue(
            method = "compile",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;cubes:Ljava/util/List;"),
            require = 0,
            expect = 0
    )
    private List<ModelPart.Cube> polydium$writeCuboids(List<ModelPart.Cube> cubes,
                                                      @Local(argsOnly = true) PoseStack.Pose pose,
                                                      @Local(argsOnly = true) VertexConsumer builder,
                                                      @Local(argsOnly = true, ordinal = 0) int lightCoords,
                                                      @Local(argsOnly = true, ordinal = 1) int overlayCoords,
                                                      @Local(argsOnly = true, ordinal = 2) int color) {
        if (!SodiumModelParts.CUBOIDS) {
            return cubes;
        }
        VertexBufferWriter writer = VertexBufferWriter.tryOf(builder/*? if < 1.21.4 {*//*, EntityVertex.FORMAT*//*?}*/);
        if (writer == null) {
            return cubes;
        }
        int abgr = ColorARGB.toABGR(color);
        for (int i = 0, n = cubes.size(); i < n; i++) {
            ModelPart.Cube cube = cubes.get(i);
            ModelCuboid cuboid = cube.getClass() == ModelPart.Cube.class ? SodiumModelParts.cuboid(cube) : null;
            if (cuboid != null) {
                EntityRenderer.renderCuboid(pose, writer, cuboid, lightCoords, overlayCoords, abgr);
            } else {
                cube.compile(pose, builder, lightCoords, overlayCoords, color);
            }
        }
        return Collections.emptyList();
    }

    @ModifyExpressionValue(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"),
            require = 0,
            expect = 0
    )
    private Collection<ModelPart> polydium$renderChildrenFromArray(Collection<ModelPart> values,
                                                                   @Local(argsOnly = true) PoseStack poseStack,
                                                                   @Local(argsOnly = true) VertexConsumer buffer,
                                                                   @Local(argsOnly = true, ordinal = 0) int lightCoords,
                                                                   @Local(argsOnly = true, ordinal = 1) int overlayCoords,
                                                                   @Local(argsOnly = true, ordinal = 2) int color) {
        if (!SodiumModelParts.CHILD_ARRAYS || this.children.getClass() != Object2ObjectArrayMap.class) {
            return values;
        }
        Object2ObjectArrayMap<?, ?> map = (Object2ObjectArrayMap<?, ?>) this.children;
        Object[] parts = SodiumModelParts.values(map);
        int size = SodiumModelParts.size(map);
        for (int i = 0; i < size; i++) {
            ((ModelPart) parts[i]).render(poseStack, buffer, lightCoords, overlayCoords, color);
        }
        return Collections.emptyList();
    }
}
