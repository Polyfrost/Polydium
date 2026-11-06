package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import org.polyfrost.polydium.early.EarlyVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(value = Options.class, priority = 900)
public abstract class MixinKeepVulkanOptions {
    private static final String KEY = "preferredGraphicsBackend";

    @Shadow
    public abstract OptionInstance<PreferredGraphicsApi> preferredGraphicsBackend();

    @WrapOperation(method = "dataFix", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag polydium$keepBackend(DataFixTypes type, DataFixer fixer, CompoundTag tag, int version, Operation<CompoundTag> original) {
        String api = EarlyVulkan.graphicsApi();
        String backend = tag.getString(KEY).orElse("vulkan".equals(api) || "opengl".equals(api) ? '"' + api + '"' : null);
        CompoundTag fixed = original.call(type, fixer, tag, version);
        if (version < EarlyVulkan.BACKEND_FIX_VERSION && backend != null && !(backend.contains("vulkan") && EarlyVulkan.failed()))
            fixed.putString(KEY, backend);
        return fixed;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polydium$removeCreatedOptions(Minecraft minecraft, File gameDirectory, CallbackInfo ci) {
        EarlyVulkan.removeCreatedOptions();
    }

    @Inject(method = "save", at = @At("HEAD"))
    private void polydium$shareBackend(CallbackInfo ci) {
        EarlyVulkan.setGraphicsApi(this.preferredGraphicsBackend().get().getSerializedName());
    }
}
//?}
