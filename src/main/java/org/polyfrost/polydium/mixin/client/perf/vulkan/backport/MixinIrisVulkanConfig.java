package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.vulkan.IrisVulkan;
import org.polyfrost.polydium.vulkan.IrisVulkanGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.compat.sodium.config.IrisConfig", remap = false)
public class MixinIrisVulkanConfig {

    @WrapMethod(method = "registerConfigLate")
    private void polydium$vulkan(ConfigBuilder builder, Operation<Void> original) {
        if (!IrisVulkanGate.applied) {
            original.call(builder);
            return;
        }
        builder.registerOwnModOptions()
                .setName("Iris")
                .setIcon(Identifier.fromNamespaceAndPath("iris", "textures/gui/config-icon-mono.png"))
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(-698654))
                .setVersion(FabricLoader.getInstance().getModContainer("iris").map(c -> c.getMetadata().getVersion().getFriendlyString().split("\\+")[0]).orElse(""))
                .addPage(builder.createExternalPage()
                        .setName(Component.translatable("options.iris.shaderPackSelection.title"))
                        .setScreenConsumer(parent -> Minecraft.getInstance().setScreen(IrisVulkan.placeholder(parent))));
    }
}
