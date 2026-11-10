package org.polyfrost.polydium.mixin.client.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.Minecraft;
//? if >= 26.2
import net.minecraft.client.gui.Gui;
//? if >= 26.3
import com.mojang.authlib.services.ProfileResult;
//? if >= 26.2 && < 26.3
//import com.mojang.authlib.yggdrasil.ProfileResult;
//? if < 26.2
//import java.util.concurrent.CompletableFuture;

//? if >= 26.2 {
@Mixin(Gui.class)
//?} else
/*@Mixin(Minecraft.class)*/
public abstract class MixinInitialScreensProfile {
    //? if >= 26.2 {
    @WrapOperation(method = "addInitialScreens", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getProfileResult"))
    private ProfileResult polydium$profileIfFetched(Minecraft minecraft, Operation<ProfileResult> original) {
        if (!PolydiumToggles.initialScreensProfile)
            return original.call(minecraft);
        return (ProfileResult) ((MinecraftProfileFutureAccessor) minecraft).polydium$profileFuture().getNow(null);
    }
    //?} else {
    /*@WrapOperation(method = "addInitialScreens", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;join()Ljava/lang/Object;"))
    private Object polydium$profileIfFetched(CompletableFuture<?> profileFuture, Operation<Object> original) {
        if (!PolydiumToggles.initialScreensProfile)
            return original.call(profileFuture);
        return profileFuture.getNow(null);
    }
    *///?}
}
