package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import org.polyfrost.polydium.client.PolydiumConstants;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(Options.class)
public abstract class MixinFpsLimitOptions {
    @Unique
    private static final Logger polydium$LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private File optionsFile;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "intValue=26"), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=options.framerateLimit"),
        to = @At(value = "CONSTANT", args = "intValue=260")))
    private int polydium$sliderSteps(int original) {
        return PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF / 10;
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "intValue=260"))
    private int polydium$codecMax(int original) {
        return PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF;
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;intRange(II)Lcom/mojang/serialization/Codec;", ordinal = 0), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=options.framerateLimit")))
    private Codec<Integer> polydium$storeVanillaLimit(Codec<Integer> original) {
        return original.xmap(value -> value, value -> Math.min(value, 260));
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void polydium$saveFramerateLimit(CallbackInfo ci) {
        int limit = ((Options) (Object) this).framerateLimit().get();
        Path path = polydium$framerateLimitFile();
        try {
            Files.writeString(path, (limit >= PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF ? "unlimited" : Integer.toString(limit)) + "\n");
        } catch (IOException e) {
            polydium$LOGGER.warn("Could not save {}", path, e);
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void polydium$loadFramerateLimit(CallbackInfo ci) {
        int stored = polydium$readFramerateLimit();
        var option = ((Options) (Object) this).framerateLimit();
        int limit = option.get();
        if (stored > 0 && (!this.optionsFile.exists() || Math.min(stored, 260) == Math.min(limit, 260))) {
            option.set(stored);
        } else if (limit >= 260) {
            option.set(PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF);
        }
    }

    @Unique
    private static int polydium$readFramerateLimit() {
        Path path = polydium$framerateLimitFile();
        if (!Files.exists(path)) return 0;
        try {
            String limit = Files.readString(path).trim();
            return limit.equals("unlimited") ? PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF : Mth.clamp(Integer.parseInt(limit), 10, PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF);
        } catch (IOException | NumberFormatException e) {
            polydium$LOGGER.warn("Could not read {}", path, e);
            return 0;
        }
    }

    @Unique
    private static Path polydium$framerateLimitFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("fpslimit.txt");
    }

    //~ if >= 26.1 '"method_42559"' -> '{"lambda$new$4", "lambda$new$5"}'
    @ModifyExpressionValue(method = {"lambda$new$4", "lambda$new$5"}, at = @At(value = "CONSTANT", args = "intValue=260"))
    private static int polydium$unlimitedLabel(int original) {
        return PolydiumConstants.UNLIMITED_FRAMERATE_CUTOFF;
    }
}
