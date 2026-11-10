package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.polyfrost.polydium.vulkan.GraphicsApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(VideoSettingsScreen.class)
public class MixinGraphicsApiOption {

    @ModifyReturnValue(method = "displayOptions", at = @At("RETURN"))
    private static OptionInstance<?>[] polydium$graphicsApi(OptionInstance<?>[] options) {
        OptionInstance<?>[] withApi = Arrays.copyOf(options, options.length + 1);
        withApi[options.length] = GraphicsApi.OPTION;
        return withApi;
    }
}
