package org.polyfrost.polydium.mixin.client.perf.iris;

//? if >= 1.21.5 {
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
//? if < 26.3 {
/*import java.util.Collection;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder", priority = 1500)
public class MixinGlCommandEncoder {
    @Unique
    private static final String POLYDIUM$IRIS_MIXIN = "net.irisshaders.iris.mixin.MixinGlCommandEncoder";
    //? if >= 26.1 {
    @Unique
    private static final String POLYDIUM$LAST_PASS = "Lcom/mojang/renderpearl/backend/opengl/GlCommandEncoder;lastPass:Lcom/mojang/renderpearl/backend/opengl/GlRenderPass;";
    @Unique
    private static final String POLYDIUM$GET_CUSTOM_PASS = "Lcom/mojang/renderpearl/backend/opengl/GlRenderPass;iris$getCustomPass()Lnet/irisshaders/iris/mixinterface/CustomPass;";
    //?} else {
    /*@Unique
    private static final String POLYDIUM$LAST_PASS = "lastPass";
    @Unique
    private static final String POLYDIUM$GET_CUSTOM_PASS = "iris$getCustomPass()Lnet/irisshaders/iris/mixinterface/CustomPass;";
    *///?}

    @Unique
    private CustomPass polydium$customPass;

    @TargetHandler(mixin = POLYDIUM$IRIS_MIXIN, name = "iris$bypassSetup")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), require = 0, expect = 0)
    //? if >= 26.3 {
    private void polydium$readCustomPass(@Coerce Object pass, CallbackInfo irisCi, CallbackInfo ci) {
    //?} else if >= 1.21.8 {
    /*private void polydium$readCustomPass(@Coerce Object pass, Collection<String> uniforms, CallbackInfoReturnable<Boolean> irisCir, CallbackInfo ci) {
    *///?} else {
    /*private void polydium$readCustomPass(@Coerce Object pass, CallbackInfoReturnable<Boolean> irisCir, CallbackInfo ci) {
    *///?}
        polydium$customPass = ((RenderPassInterface) pass).iris$getCustomPass();
    }

    //? if >= 26.2 {
    @TargetHandler(mixin = POLYDIUM$IRIS_MIXIN, name = "iris$bypassSetup")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/Iris;isPackInUseQuick()Z"), require = 0, expect = 0)
    private boolean polydium$customPassFirst(Operation<Boolean> original) {
        return polydium$customPass != null || original.call();
    }
    //?}

    @TargetHandler(mixin = POLYDIUM$IRIS_MIXIN, name = "iris$bypassSetup")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = POLYDIUM$GET_CUSTOM_PASS, remap = false), require = 0, expect = 0)
    private CustomPass polydium$cachedCustomPass(@Coerce Object pass, Operation<CustomPass> original) {
        return polydium$customPass;
    }

    @TargetHandler(mixin = POLYDIUM$IRIS_MIXIN, name = "iris$bypassSetup")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "FIELD", target = POLYDIUM$LAST_PASS, opcode = Opcodes.PUTSTATIC, remap = false), require = 0, expect = 0)
    private void polydium$dropLastPass(@Coerce Object pass, Operation<Void> original) {
    }

    //? if = 26.2 {
    /*
    @Unique
    private boolean polydium$drawBuffersSet;

    @TargetHandler(mixin = POLYDIUM$IRIS_MIXIN, name = "iris$skipShadowDrawBuffers")
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
