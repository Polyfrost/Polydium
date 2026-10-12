package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.4 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(Model.class)
public abstract class MixinModelPoseReset {
    @Unique
    private ModelPart[] polydium$parts;

    @WrapOperation(method = "resetPose", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<ModelPart> polydium$resetFromArray(List<ModelPart> allParts, Operation<Iterator<ModelPart>> original) {
        ModelPart[] parts = this.polydium$parts;
        if (parts == null) {
            this.polydium$parts = parts = allParts.toArray(new ModelPart[0]);
        }
        for (ModelPart part : parts) {
            part.resetPose();
        }
        return Collections.emptyIterator();
    }
}
//?}
