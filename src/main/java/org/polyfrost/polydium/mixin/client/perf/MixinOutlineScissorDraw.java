package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(StagedVertexBuffer.Draw.class)
public abstract class MixinOutlineScissorDraw implements OutlineScissor.Draw {
    @Unique
    private float[] polydium$outlineBoxes;

    @Unique
    private int polydium$outlineBoxValues;

    @Override
    public void polydium$trackOutline() {
        this.polydium$outlineBoxes = new float[OutlineScissor.BOX_VALUES * 2];
    }

    @Override
    public float[] polydium$outlineBoxes() {
        return this.polydium$outlineBoxes;
    }

    @Override
    public int polydium$outlineBoxValues() {
        return this.polydium$outlineBoxValues;
    }

    @Inject(method = "append", at = @At("RETURN"))
    private void polydium$measureOutline(MeshData mesh, CallbackInfo ci) {
        float[] boxes = this.polydium$outlineBoxes;
        if (boxes == null)
            return;
        if (this.polydium$outlineBoxValues == boxes.length)
            this.polydium$outlineBoxes = boxes = Arrays.copyOf(boxes, boxes.length * 2);
        OutlineScissor.measureVertices(mesh, boxes, this.polydium$outlineBoxValues);
        this.polydium$outlineBoxValues += OutlineScissor.BOX_VALUES;
    }
}
//?}
