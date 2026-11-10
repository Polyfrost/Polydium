package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "co.stellarskys.stella.api.lumina.LuminaPIPRenderer",
    "co.stellarskys.stella.api.lumina.renderer.gl.GLBackend",
    "co.stellarskys.stella.api.lumina.renderer.gl.GLShapeRenderer",
    "co.stellarskys.stella.api.lumina.renderer.gl.GLTextureRenderer",
    "co.stellarskys.stella.api.lumina.renderer.gl.GLUtils",
    "co.stellarskys.stella.api.lumina.renderer.gl.GLUtils$GLState"
}, remap = false)
public class MixinVulkanStella {
}
