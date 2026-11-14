package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 1.21.11 && < 26.2 {
/*import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.OutlineBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.shadows.ShadowRenderer", remap = false)
public abstract class MixinIrisShadowOutlineDrop {
    @Shadow
    @Final
    private OutlineBufferSource outlineBuffers;

    @Inject(method = "renderShadows", at = @At("RETURN"), require = 0)
    private void polydium$dropShadowOutlines(CallbackInfo ci) {
        BufferSourceAccessor source = (BufferSourceAccessor) ((OutlineBufferSourceAccessor) this.outlineBuffers).polydium$bufferSource();
        Map<?, BufferBuilder> started = source.polydium$startedBuilders();
        for (BufferBuilder builder : started.values()) {
            MeshData mesh = builder.build();
            if (mesh != null)
                mesh.close();
        }
        started.clear();
        source.polydium$setLastSharedType(null);
    }
}
*///?} else {
public abstract class MixinIrisShadowOutlineDrop {
}
//?}
