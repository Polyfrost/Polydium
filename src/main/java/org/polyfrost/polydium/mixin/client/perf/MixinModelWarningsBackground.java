package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.model.ModelManager;
//? if >= 26.2
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import org.polyfrost.polydium.client.perf.BackgroundCleanup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if < 26.2 {
/*import java.util.Map;
import java.util.function.BiConsumer;
*///?}

@Mixin(ModelManager.class)
public abstract class MixinModelWarningsBackground {
    //? if >= 26.2 {
    @WrapOperation(
        method = "lambda$loadModels$1",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/sprite/MaterialBaker;logMissingTextures()V")
    )
    private static void polydium$logMissingTexturesInBackground(MaterialBaker materialBaker, Operation<Void> original) {
        BackgroundCleanup.run(() -> original.call(materialBaker));
    }
    //?} else {
    /*@WrapOperation(
        method = /^? if >= 26.1 {^/"lambda$loadModels$1"/^?} else {^//^"method_68047"^//^?}^/,
        at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V")
    )
    private static void polydium$logMissingTexturesInBackground(Map<?, ?> missing, BiConsumer<?, ?> log, Operation<Void> original) {
        BackgroundCleanup.run(() -> original.call(missing, log));
    }
    *///?}
}
//?}
