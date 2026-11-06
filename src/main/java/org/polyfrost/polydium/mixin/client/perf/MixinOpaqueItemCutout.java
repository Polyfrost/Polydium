package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.5 && < 26.1 {
/*import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.SpriteTransparency;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
//? if >= 1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
//?} else {
/^import net.minecraft.client.renderer.RenderType;
^///?}

import java.util.List;

@Mixin(BlockModelWrapper.class)
public abstract class MixinOpaqueItemCutout {
    //? if >= 1.21.11 {
    @Unique
    private static final RenderType POLYDIUM_CUTOUT = RenderTypes.entityCutout(TextureAtlas.LOCATION_ITEMS);
    //?} else {
    /^@Unique
    private static final RenderType POLYDIUM_CUTOUT = Sheets.cutoutBlockSheet();
    ^///?}

    @Shadow
    @Final
    private List<BakedQuad> quads;

    @Unique
    private byte polydium$translucent;

    //? if >= 1.21.11 {
    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;setRenderType(Lnet/minecraft/client/renderer/rendertype/RenderType;)V"))
    //?} else {
    /^@ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;setRenderType(Lnet/minecraft/client/renderer/RenderType;)V"))
    ^///?}
    private RenderType polydium$cutoutOpaqueItem(RenderType type) {
        if (!PolydiumToggles.opaqueItemCutout || type != Sheets.translucentItemSheet()) {
            return type;
        }
        byte state = this.polydium$translucent;
        if (state == 0) {
            state = this.quads.isEmpty() ? (byte) 1 : (byte) -1;
            for (BakedQuad quad : this.quads) {
                if (SpriteTransparency.hasTranslucent(quad.sprite())) {
                    state = 1;
                    break;
                }
            }
            this.polydium$translucent = state;
        }
        return state > 0 ? type : POLYDIUM_CUTOUT;
    }
}
*///?}
