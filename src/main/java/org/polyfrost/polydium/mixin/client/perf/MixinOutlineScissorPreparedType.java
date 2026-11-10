package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.joml.Matrix4fc;
import org.polyfrost.polydium.client.perf.OutlineScissor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PreparedRenderType.class)
public abstract class MixinOutlineScissorPreparedType implements OutlineScissor.ModelView {
    @Unique
    private Matrix4fc polydium$modelView;

    @Override
    public Matrix4fc polydium$modelView() {
        return this.polydium$modelView;
    }

    @Override
    public void polydium$setModelView(Matrix4fc modelView) {
        this.polydium$modelView = modelView;
    }
}
//?}
