package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

//? if >= 26.1 {
import net.minecraft.client.renderer.Lightmap;
//?} else
/*import net.minecraft.client.renderer.LightTexture;*/
import org.spongepowered.asm.mixin.Mixin;

//? if >= 26.1 {
@Mixin(value = Lightmap.class, priority = 1500)
//?} else
/*@Mixin(value = LightTexture.class, priority = 1500)*/
public class MixinVulkanDhLightmap {
}
