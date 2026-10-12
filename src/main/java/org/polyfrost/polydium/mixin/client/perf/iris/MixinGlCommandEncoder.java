package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 26.2 {
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.mixinterface.CustomPass;
import net.irisshaders.iris.mixinterface.RenderPassInterface;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if = 26.2 {
/*import java.util.Collection;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder", priority = 1500)
public class MixinGlCommandEncoder {
    private static final String IRIS_MIXIN = "net.irisshaders.iris.mixin.MixinGlCommandEncoder";
    private static final String LAST_PASS = "Lcom/mojang/renderpearl/backend/opengl/GlCommandEncoder;lastPass:Lcom/mojang/renderpearl/backend/opengl/GlRenderPass;";
    private static final String GET_CUSTOM_PASS = "Lcom/mojang/renderpearl/backend/opengl/GlRenderPass;iris$getCustomPass()Lnet/irisshaders/iris/mixinterface/CustomPass;";

    @Unique
    private CustomPass polydium$customPass;

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$bypassSetup")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), require = 0, expect = 0)
    //? if >= 26.3 {
    private void polydium$readCustomPass(@Coerce Object pass, CallbackInfo irisCi, CallbackInfo ci) {
    //?} else {
    /*private void polydium$readCustomPass(@Coerce Object pass, Collection<String> uniforms, CallbackInfoReturnable<Boolean> irisCir, CallbackInfo ci) {
    *///?}
        polydium$customPass = ((RenderPassInterface) pass).iris$getCustomPass();
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$bypassSetup")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/Iris;isPackInUseQuick()Z"), require = 0, expect = 0)
    private boolean polydium$customPassFirst(Operation<Boolean> original) {
        return polydium$customPass != null || original.call();
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$bypassSetup")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = GET_CUSTOM_PASS), require = 0, expect = 0)
    private CustomPass polydium$cachedCustomPass(@Coerce Object pass, Operation<CustomPass> original) {
        return polydium$customPass;
    }

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$bypassSetup")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "FIELD", target = LAST_PASS, opcode = Opcodes.PUTSTATIC), require = 0, expect = 0)
    private void polydium$dropLastPass(@Coerce Object pass, Operation<Void> original) {
    }

    //? if = 26.2 {
    /*
    @Unique
    private boolean polydium$drawBuffersSet;

    @TargetHandler(mixin = IRIS_MIXIN, name = "iris$skipShadowDrawBuffers")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL33C;glDrawBuffers([I)V"), require = 0, expect = 0)
    private void polydium$drawBuffersOncePerPass(int[] buffers, Operation<Void> original) {
        if (Iris.isPackInUseQuick() || ImmediateState.safeToMultiply) {
            polydium$drawBuffersSet = false;
        } else if (polydium$drawBuffersSet) {
            return;
        } else {
            polydium$drawBuffersSet = true;
        }

        original.call(buffers);
    }

    @Inject(method = "createRenderPass", at = @At("HEAD"))
    private void polydium$resetDrawBuffers(CallbackInfoReturnable<?> cir) {
        polydium$drawBuffersSet = false;
    }
    *///?}
}
//?}
