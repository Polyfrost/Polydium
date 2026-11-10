package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.polyfrost.polydium.client.PolydiumToggles;
import org.polyfrost.polydium.client.perf.AnimatedSpriteOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >= 26.2 {
import com.google.common.collect.ImmutableList;
//?} else {
/*import java.util.List;
*///?}

@Mixin(TextureAtlas.class)
public abstract class MixinAnimatedSpriteOrder {
    //? if >= 26.2 {
    @ModifyExpressionValue(method = "upload", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;", ordinal = 1))
    private ImmutableList<SpriteContents.AnimationState> polydium$groupByPipeline(ImmutableList<SpriteContents.AnimationState> states) {
    //?} else {
    /*@ModifyExpressionValue(method = "upload", at = @At(value = "INVOKE", target = "Ljava/util/List;copyOf(Ljava/util/Collection;)Ljava/util/List;"))
    private List<SpriteContents.AnimationState> polydium$groupByPipeline(List<SpriteContents.AnimationState> states) {
    *///?}
        return PolydiumToggles.animatedSpriteOrder ? AnimatedSpriteOrder.group(states) : states;
    }
}
//?}
