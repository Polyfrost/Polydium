package org.polyfrost.polydium.mixin.client.perf.iris;

import net.irisshaders.iris.uniforms.IrisTimeUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.IrisTimeUniforms", remap = false)
public class MixinIrisTimeUniforms {
    static {
        IrisTimeUniforms.updateTime();
    }
}
