package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinEntityOutlineGate {

    @Unique
    private boolean polydium$anyOutline;

    @ModifyExpressionValue(method = "lambda$addMainPass$0", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;shouldShowEntityOutlines:Z", opcode = Opcodes.GETFIELD))
    private boolean polydium$clearOnlyWithOutlines(boolean show, @Local(argsOnly = true) FeatureRenderDispatcher.PreparedFrame featureFrame) {
        this.polydium$anyOutline = featureFrame.hasAnyOutline();
        return show && this.polydium$anyOutline;
    }

    @ModifyExpressionValue(method = "doEntityOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;shouldShowEntityOutlines:Z", opcode = Opcodes.GETFIELD))
    private boolean polydium$blitOnlyWithOutlines(boolean show) {
        return show && this.polydium$anyOutline;
    }
}
*///?} else if >= 1.21.10 && < 26.2 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state./^? if >= 26.1 {^/level./^?}^/LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinEntityOutlineGate {

    @Shadow @Final private LevelRenderState levelRenderState;

    @Unique
    private boolean polydium$anyOutline;

    @ModifyExpressionValue(
            method = /^? if >= 26.1 {^/"lambda$addMainPass$0"/^?} else {^//^"method_62214"^//^?}^/,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z")
    )
    private boolean polydium$clearOnlyWithOutlines(boolean show) {
        this.polydium$anyOutline = this.levelRenderState.haveGlowingEntities;
        return show && this.polydium$anyOutline;
    }

    @ModifyExpressionValue(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"))
    private boolean polydium$blitOnlyWithOutlines(boolean show) {
        return show && this.polydium$anyOutline;
    }
}
*///?} else if >= 1.21.4 && < 1.21.10 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinEntityOutlineGate {

    @Unique
    private boolean polydium$anyOutline;

    @ModifyReturnValue(method = "collectVisibleEntities", at = @At("RETURN"))
    private boolean polydium$captureGlowing(boolean glowing) {
        this.polydium$anyOutline = glowing;
        return glowing;
    }

    @ModifyExpressionValue(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"))
    private boolean polydium$clearOnlyWithOutlines(boolean show) {
        return show && this.polydium$anyOutline;
    }

    @ModifyExpressionValue(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"))
    private boolean polydium$blitOnlyWithOutlines(boolean show) {
        return show && this.polydium$anyOutline;
    }
}
*///?} else if < 1.21.4 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinEntityOutlineGate {

    @Unique
    private boolean polydium$anyOutline;

    @Unique
    private boolean polydium$outlineDirty = true;

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z", ordinal = 0))
    private boolean polydium$clearOnlyWhenDirty(boolean show) {
        this.polydium$anyOutline = false;
        if (!show) {
            return false;
        }
        if (!this.polydium$outlineDirty) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            return false;
        }
        this.polydium$outlineDirty = false;
        return true;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V", ordinal = 0))
    private void polydium$markOutlineDrawn(CallbackInfo ci) {
        this.polydium$anyOutline = true;
        this.polydium$outlineDirty = true;
    }

    @ModifyExpressionValue(method = "doEntityOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"))
    private boolean polydium$blitOnlyWithOutlines(boolean show) {
        return show && this.polydium$anyOutline;
    }
}
*///?}
