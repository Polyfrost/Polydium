package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 || < 1.21.4 {
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.quad.FullTQuad;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.quad.FullTQuad", remap = false)
public abstract class MixinFullTQuadPackedVertices {
    @Unique
    private static final int STRIDE = 8;

    @Shadow
    @Final
    @Mutable
    private ChunkVertexEncoder.Vertex[] vertices;

    @Unique
    private int[] polydium$packed;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;uninitializedQuad()[Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;"))
    private ChunkVertexEncoder.Vertex[] polydium$deferVertices() {
        return null;
    }

    @Inject(method = "initVertices", at = @At("HEAD"), cancellable = true)
    private void polydium$packVertices(ChunkVertexEncoder.Vertex[] source, CallbackInfo ci) {
        if (this.vertices != null) {
            return;
        }
        int[] packed = new int[4 * STRIDE];
        for (int i = 0, o = 0; i < 4; i++, o += STRIDE) {
            ChunkVertexEncoder.Vertex v = source[i];
            packed[o] = Float.floatToRawIntBits(v.x);
            packed[o + 1] = Float.floatToRawIntBits(v.y);
            packed[o + 2] = Float.floatToRawIntBits(v.z);
            packed[o + 3] = v.color;
            packed[o + 4] = Float.floatToRawIntBits(v.ao);
            packed[o + 5] = Float.floatToRawIntBits(v.u);
            packed[o + 6] = Float.floatToRawIntBits(v.v);
            packed[o + 7] = v.light;
        }
        this.polydium$packed = packed;
        ci.cancel();
    }

    @Inject(method = "getVertexPositions", at = @At("HEAD"))
    private void polydium$packedPositions(CallbackInfoReturnable<float[]> cir) {
        int[] packed = this.polydium$packed;
        RegularTQuadAccessor self = (RegularTQuadAccessor) this;
        if (packed == null || self.polydium$vertexPositions() != null) {
            return;
        }
        float[] positions = new float[12];
        for (int i = 0, o = 0; i < 4; i++, o += STRIDE) {
            positions[i * 3] = Float.intBitsToFloat(packed[o]);
            positions[i * 3 + 1] = Float.intBitsToFloat(packed[o + 1]);
            positions[i * 3 + 2] = Float.intBitsToFloat(packed[o + 2]);
        }
        self.polydium$setVertexPositions(positions);
    }

    @Redirect(method = {"initVertices", "writeToBuffer", "getVertexPositions", "getVeryAccurateNormal", "getVertices", "updateSplitQuadAfterVertexModification"},
            at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/quad/FullTQuad;vertices:[Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;", opcode = 180))
    private ChunkVertexEncoder.Vertex[] polydium$vertices(FullTQuad quad) {
        return polydium$unpack(quad);
    }

    @Redirect(method = "splittingCopy",
            at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/quad/FullTQuad;vertices:[Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;", opcode = 180))
    private static ChunkVertexEncoder.Vertex[] polydium$copySourceVertices(FullTQuad quad) {
        return polydium$unpack(quad);
    }

    @Unique
    private static ChunkVertexEncoder.Vertex[] polydium$unpack(FullTQuad quad) {
        MixinFullTQuadPackedVertices self = (MixinFullTQuadPackedVertices) (Object) quad;
        ChunkVertexEncoder.Vertex[] vertices = self.vertices;
        if (vertices != null) {
            return vertices;
        }
        int[] packed = self.polydium$packed;
        vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();
        for (int i = 0, o = 0; i < 4; i++, o += STRIDE) {
            ChunkVertexEncoder.Vertex v = vertices[i];
            v.x = Float.intBitsToFloat(packed[o]);
            v.y = Float.intBitsToFloat(packed[o + 1]);
            v.z = Float.intBitsToFloat(packed[o + 2]);
            v.color = packed[o + 3];
            v.ao = Float.intBitsToFloat(packed[o + 4]);
            v.u = Float.intBitsToFloat(packed[o + 5]);
            v.v = Float.intBitsToFloat(packed[o + 6]);
            v.light = packed[o + 7];
        }
        self.vertices = vertices;
        self.polydium$packed = null;
        return vertices;
    }
}
//?}
