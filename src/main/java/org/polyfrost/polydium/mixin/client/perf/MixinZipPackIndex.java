package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import org.polyfrost.polydium.client.perf.ZipPackIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(FilePackResources.class)
public abstract class MixinZipPackIndex {
    @Shadow
    protected abstract String addPrefix(String path);

    @WrapOperation(method = "listResources", at = @At(value = "INVOKE", target = "Ljava/util/zip/ZipFile;entries()Ljava/util/Enumeration;"))
    private Enumeration<? extends ZipEntry> polydium$listFromIndex(
        ZipFile zipFile,
        Operation<Enumeration<? extends ZipEntry>> original,
        @Local(argsOnly = true) PackType type,
        @Local(argsOnly = true, ordinal = 0) String namespace,
        @Local(argsOnly = true, ordinal = 1) String directory
    ) {
        if (!ZipPackIndex.AVAILABLE) {
            return original.call(zipFile);
        }
        return ZipPackIndex.entries(zipFile, this.addPrefix(type.getDirectory() + "/" + namespace + "/") + directory + "/");
    }
}
//?}
