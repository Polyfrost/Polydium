package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 26.3 {
import net.minecraft.server.packs.FixedPathPackResources;
//?} else {
/*import net.minecraft.server.packs.VanillaPackResources;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.LinkOption;
import java.nio.file.Path;

//? if >= 26.3 {
@Mixin(FixedPathPackResources.class)
//?} else {
/*@Mixin(VanillaPackResources.class)
*///?}
public abstract class MixinLinkFsExists {
    @WrapOperation(
            method = /*? if >= 26.1 {*/"lambda$getResource$0"/*?} else {*//*"method_45189"*//*?}*/,
            at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;exists(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z")
    )
    private boolean polydium$existsWithoutThrow(Path path, LinkOption[] options, Operation<Boolean> original) {
        if (options.length == 0 && path instanceof MixinLinkFsExistsPath link) {
            return link.polydium$exists();
        }
        return original.call(path, options);
    }
}
