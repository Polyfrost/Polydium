package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import org.polyfrost.polydium.client.perf.SectionBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder", remap = false)
public abstract class MixinOcclusionBoundsVertex implements SectionBounds.PassHolder {
    @Unique
    private int polydium$pass;
    @Unique
    private boolean polydium$translucent;
    @Unique
    private SectionBounds.Tracked polydium$tracked;

    @ModifyVariable(method = "push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;I)V", at = @At("HEAD"), argsOnly = true)
    private ChunkVertexEncoder.Vertex[] polydium$trackBounds(ChunkVertexEncoder.Vertex[] vertices, @Local(argsOnly = true) int materialBits) {
        if (this.polydium$tracked == null)
            this.polydium$tracked = SectionBounds.tracked();

        if (this.polydium$tracked.active())
            SectionBounds.include(this.polydium$tracked, vertices, materialBits, this.polydium$pass, this.polydium$translucent);

        return vertices;
    }

    @Override
    public void polydium$setPass(int pass, boolean translucent) {
        this.polydium$pass        = pass;
        this.polydium$translucent = translucent;
    }
}
//?}
