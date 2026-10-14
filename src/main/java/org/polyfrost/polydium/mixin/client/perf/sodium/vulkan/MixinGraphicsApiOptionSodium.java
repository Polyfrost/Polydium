package org.polyfrost.polydium.mixin.client.perf.sodium.vulkan;

import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.vulkan.GraphicsApi;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SodiumConfigBuilder.class, remap = false)
public class MixinGraphicsApiOptionSodium {

    @Shadow
    @Final
    private StorageEventHandler vanillaStorage;

    @Inject(method = "buildGeneralPage", at = @At("RETURN"))
    private void polydium$graphicsApi(ConfigBuilder builder, CallbackInfoReturnable<OptionPageBuilder> cir) {
        cir.getReturnValue().addOptionGroup(builder.createOptionGroup().addOption(
                builder.createEnumOption(Identifier.fromNamespaceAndPath("sodium", "general.graphics_api"), GraphicsApi.class)
                        .setStorageHandler(this.vanillaStorage)
                        .setName(Component.translatable("options.graphicsApi"))
                        .setTooltip(api -> Component.translatable(api == GraphicsApi.VULKAN ? "options.graphicsApi.tooltip.vulkan" : "options.graphicsApi.tooltip"))
                        .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                                Component.translatable("options.graphicsApi.default"),
                                Component.translatable("options.graphicsApi.opengl"),
                                Component.translatable("options.graphicsApi.vulkan")))
                        .setDefaultValue(GraphicsApi.DEFAULT)
                        .setFlags(OptionFlag.REQUIRES_GAME_RESTART)
                        .setBinding(GraphicsApi.OPTION::set, GraphicsApi.OPTION::get)));
    }
}
