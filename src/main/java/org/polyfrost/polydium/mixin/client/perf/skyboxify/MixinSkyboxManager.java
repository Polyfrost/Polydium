package org.polyfrost.polydium.mixin.client.perf.skyboxify;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//? if skyboxify {
import btw.lowercase.skyboxify.skybox.impl.Skybox;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
//?}

@Pseudo
@Mixin(targets = "btw.lowercase.skyboxify.skybox.SkyboxManager", remap = false)
public class MixinSkyboxManager {
    @Unique
    private final Matrix4f polydium$modelView = new Matrix4f();

    @WrapOperation(
            method = "extractSkyboxes",
            at = @At(value = "NEW", target = "()Lorg/joml/Matrix4f;"),
            require = 0
    )
    private Matrix4f polydium$reuseModelView(Operation<Matrix4f> original) {
        return this.polydium$modelView.identity();
    }

    //? if skyboxify {
    @Shadow
    @Final
    private List<Skybox> activeSkies;

    // Same result as the original without streaming into a throwaway list
    @WrapMethod(method = "containsEnabled", require = 0)
    private boolean polydium$containsEnabled(ResourceKey<Level> resourceKey, Operation<Boolean> original) {
        for (Skybox skybox : this.activeSkies) {
            if (resourceKey.equals(skybox.dimension())) {
                return true;
            }
        }

        return false;
    }
    //?}
}
