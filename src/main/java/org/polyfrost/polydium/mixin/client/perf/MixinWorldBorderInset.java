package org.polyfrost.polydium.mixin.client.perf;


//? if >= 26.1 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.WorldBorderRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldBorderRenderer.class)
public abstract class MixinWorldBorderInset {

    @ModifyExpressionValue(method = {"rebuildWorldBorderBuffer", "shouldRebuildWorldBorderBuffer"}, at = {
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;minX:D", opcode = Opcodes.GETFIELD),
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;minZ:D", opcode = Opcodes.GETFIELD)
    })
    private double polydium$insetMin(double min) {
        return min + 0.01;
    }

    @ModifyExpressionValue(method = {"rebuildWorldBorderBuffer", "shouldRebuildWorldBorderBuffer"}, at = {
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;maxX:D", opcode = Opcodes.GETFIELD),
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;maxZ:D", opcode = Opcodes.GETFIELD)
    })
    private double polydium$insetMax(double max) {
        return max - 0.01;
    }
}
*///?} else if >= 1.21.10 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.WorldBorderRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldBorderRenderer.class)
public abstract class MixinWorldBorderInset {

    @ModifyExpressionValue(method = {"rebuildWorldBorderBuffer", "shouldRebuildWorldBorderBuffer"}, at = {
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WorldBorderRenderState;minX:D", opcode = Opcodes.GETFIELD),
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WorldBorderRenderState;minZ:D", opcode = Opcodes.GETFIELD)
    })
    private double polydium$insetMin(double min) {
        return min + 0.01;
    }

    @ModifyExpressionValue(method = {"rebuildWorldBorderBuffer", "shouldRebuildWorldBorderBuffer"}, at = {
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WorldBorderRenderState;maxX:D", opcode = Opcodes.GETFIELD),
        @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WorldBorderRenderState;maxZ:D", opcode = Opcodes.GETFIELD)
    })
    private double polydium$insetMax(double max) {
        return max - 0.01;
    }
}
*///?} else if >= 1.21.5 && < 1.21.10 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.WorldBorderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldBorderRenderer.class)
public abstract class MixinWorldBorderInset {

    @ModifyExpressionValue(method = {"rebuildWorldBorderBuffer", "shouldRebuildWorldBorderBuffer"}, at = {
        @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMinX()D"),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMinZ()D")
    })
    private double polydium$insetMin(double min) {
        return min + 0.01;
    }

    @ModifyExpressionValue(method = {"rebuildWorldBorderBuffer", "shouldRebuildWorldBorderBuffer"}, at = {
        @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMaxX()D"),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMaxZ()D")
    })
    private double polydium$insetMax(double max) {
        return max - 0.01;
    }
}
*///?} else if >= 1.21.4 && < 1.21.5 {
/*import net.minecraft.client.renderer.WorldBorderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldBorderRenderer.class)
public abstract class MixinWorldBorderInset {

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;getInstance()Lcom/mojang/blaze3d/vertex/Tesselator;"), index = 7)
    private double polydium$insetMinX(double min) {
        return min + 0.01;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;getInstance()Lcom/mojang/blaze3d/vertex/Tesselator;"), index = 9)
    private double polydium$insetMaxX(double max) {
        return max - 0.01;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;getInstance()Lcom/mojang/blaze3d/vertex/Tesselator;"), index = 11)
    private double polydium$insetMinZ(double min) {
        return min + 0.01;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;getInstance()Lcom/mojang/blaze3d/vertex/Tesselator;"), index = 13)
    private double polydium$insetMaxZ(double max) {
        return max - 0.01;
    }
}
*///?} else if < 1.21.4 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LevelRenderer.class)
public abstract class MixinWorldBorderInset {

    @ModifyExpressionValue(
        method = "renderWorldBorder",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getDistanceToBorder(DD)D")),
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMinX()D"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMinZ()D")
        }
    )
    private double polydium$insetMin(double min) {
        return min + 0.01;
    }

    @ModifyExpressionValue(
        method = "renderWorldBorder",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getDistanceToBorder(DD)D")),
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMaxX()D"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getMaxZ()D")
        }
    )
    private double polydium$insetMax(double max) {
        return max - 0.01;
    }
}
*///?}
