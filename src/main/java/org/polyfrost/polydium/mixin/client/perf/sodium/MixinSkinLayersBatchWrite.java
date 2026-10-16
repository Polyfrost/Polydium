package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 || < 1.21.4 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.perf.DirectVertexBuffers;
import org.polyfrost.polydium.client.perf.SkinLayerMeshes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "dev.tr7zw.skinlayers.render.CustomizableModelPart", remap = false)
public abstract class MixinSkinLayersBatchWrite {
    @WrapMethod(method = "compile", require = 0, expect = 0)
    private void polydium$writeMesh(ModelPart vanillaModel, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int color,
                                    Operation<Void> original) {
        float[] data = SkinLayerMeshes.polygons(this);
        if (data == null || data.length == 0) {
            original.call(vanillaModel, pose, consumer, light, overlay, color);
            return;
        }
        int quads = data.length / SkinLayerMeshes.QUAD_FLOATS;
        long pointer = DirectVertexBuffers.reserve(consumer, EntityVertex.FORMAT, EntityVertex.STRIDE, quads * 4);
        if (pointer == 0L) {
            original.call(vanillaModel, pose, consumer, light, overlay, color);
            return;
        }
        polydium$writePolygons(data, pointer, pose, light, overlay, color);
        for (Object cube : SkinLayerMeshes.cubes(this)) {
            ((ModelPart.Cube) cube).compile(pose, consumer, light, overlay, color);
        }
    }

    @Unique
    private static void polydium$writePolygons(float[] data, long pointer, PoseStack.Pose pose, int light, int overlay, int color) {

        Matrix4f matrix = pose.pose();
        float m00 = matrix.m00(), m01 = matrix.m01(), m02 = matrix.m02();
        float m10 = matrix.m10(), m11 = matrix.m11(), m12 = matrix.m12();
        float m20 = matrix.m20(), m21 = matrix.m21(), m22 = matrix.m22();
        float m30 = matrix.m30(), m31 = matrix.m31(), m32 = matrix.m32();
        Matrix3f normalMatrix = pose.normal();
        float n00 = normalMatrix.m00(), n01 = normalMatrix.m01(), n02 = normalMatrix.m02();
        float n10 = normalMatrix.m10(), n11 = normalMatrix.m11(), n12 = normalMatrix.m12();
        float n20 = normalMatrix.m20(), n21 = normalMatrix.m21(), n22 = normalMatrix.m22();
        int abgr = ColorARGB.toABGR(color);

        for (int quad = 0; quad < data.length; quad += SkinLayerMeshes.QUAD_FLOATS) {
            float nx = data[quad], ny = data[quad + 1], nz = data[quad + 2];
            int normal = NormI8.pack(
                    n00 * nx + n10 * ny + n20 * nz,
                    n01 * nx + n11 * ny + n21 * nz,
                    n02 * nx + n12 * ny + n22 * nz
            );
            for (int vertex = quad + 3; vertex < quad + SkinLayerMeshes.QUAD_FLOATS; vertex += 5) {
                float x = data[vertex], y = data[vertex + 1], z = data[vertex + 2];
                EntityVertex.write(pointer,
                        m00 * x + m10 * y + m20 * z + m30,
                        m01 * x + m11 * y + m21 * z + m31,
                        m02 * x + m12 * y + m22 * z + m32,
                        abgr, data[vertex + 3], data[vertex + 4], overlay, light, normal);
                pointer += EntityVertex.STRIDE;
            }
        }
    }
}
//?}
