package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.frontend.shaders.SPIRVModule;
import org.polyfrost.polydium.client.perf.BackgroundCleanup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SPIRVModule.class)
public abstract class MixinShaderModuleBackgroundClose {
    @WrapMethod(method = "close")
    private void polydium$closeInBackground(Operation<Void> original) {
        BackgroundCleanup.run(original::call);
    }
}
//?}
