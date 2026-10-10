package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinAtlasScratchView {
    @Unique
    private GpuTexture polydium$scratchTexture;

    @Unique
    private GpuTextureView polydium$scratchView;

    @WrapOperation(
        method = "uploadInitialContents",
        at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuDevice;createTextureView(Lcom/mojang/renderpearl/api/textures/GpuTexture;)Lcom/mojang/renderpearl/api/textures/GpuTextureView;")
    )
    private GpuTextureView polydium$shareScratchView(GpuDevice device, GpuTexture scratchTexture, Operation<GpuTextureView> original) {
        if (scratchTexture == this.polydium$scratchTexture) {
            return this.polydium$scratchView;
        }
        GpuTextureView view = original.call(device, scratchTexture);
        this.polydium$scratchTexture = scratchTexture;
        this.polydium$scratchView = view;
        return view;
    }

    @Inject(method = "uploadInitialContents", at = @At("TAIL"))
    private void polydium$forgetScratchView(CallbackInfo ci) {
        this.polydium$scratchTexture = null;
        this.polydium$scratchView = null;
    }
}
//?}
