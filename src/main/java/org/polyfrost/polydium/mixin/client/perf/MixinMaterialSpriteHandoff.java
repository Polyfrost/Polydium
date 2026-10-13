package org.polyfrost.polydium.mixin.client.perf;

//? if < 1.21.10 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import org.polyfrost.polydium.client.perf.SpriteHandoff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Material.class)
public abstract class MixinMaterialSpriteHandoff implements SpriteHandoff {

    @Unique
    private TextureAtlasSprite polydium$handed;

    @Override
    public void polydium$handSprite(TextureAtlasSprite sprite) {
        this.polydium$handed = sprite;
    }

    @WrapOperation(method = "buffer(Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite polydium$handedSprite(Material material, Operation<TextureAtlasSprite> original) {
        TextureAtlasSprite handed = this.polydium$handed;
        return handed != null ? handed : original.call(material);
    }
}
*///?}
