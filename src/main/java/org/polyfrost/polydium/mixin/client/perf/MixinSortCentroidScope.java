package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if < 26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.polyfrost.polydium.client.perf.SortCentroidScope;
import org.spongepowered.asm.mixin.injection.At;
*///?}

@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.MultiBufferSource$BufferSource")
public class MixinSortCentroidScope {
    //? if < 26.2 {
    /*@WrapOperation(
        method = /^? if >= 1.21.11 {^/"endBatch(Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V"/^?} else {^//^"endBatch(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V"^//^?}^/,
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/MeshData;sortQuads(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;Lcom/mojang/blaze3d/vertex/VertexSorting;)Lcom/mojang/blaze3d/vertex/MeshData$SortState;")
    )
    private MeshData.SortState polydium$markDiscarded(MeshData mesh, ByteBufferBuilder buffer, VertexSorting sorting, Operation<MeshData.SortState> original) {
        SortCentroidScope.discarded = true;
        try {
            return original.call(mesh, buffer, sorting);
        } finally {
            SortCentroidScope.discarded = false;
        }
    }
    *///?}
}
//?}
