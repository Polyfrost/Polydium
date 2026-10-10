package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(SubmitNodeStorage.class)
public class MixinSubmitCollectionReuse {
    @WrapOperation(
        method = /*? if >= 26.2 {*/"drainPhases"/*?} else {*//*"endFrame"*//*?}*/,
        at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectCollection;removeIf(Ljava/util/function/Predicate;)Z")
    )
    private boolean polydium$drainWithoutRemoving(ObjectCollection<SubmitNodeCollection> collections, Predicate<SubmitNodeCollection> drain, Operation<Boolean> original) {
        if (collections.size() > 64) {
            return original.call(collections, drain);
        }
        for (SubmitNodeCollection collection : collections) {
            drain.test(collection);
        }
        return false;
    }
}
//?}
