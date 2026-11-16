package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if < 26.1 {
/*import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
*///?}
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.perf.SpriteSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
//? if >= 26.1 {
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
public abstract class MixinSpriteSetRegion implements SpriteSet.Access {
    //? if >= 26.1 {
    @Shadow @Final private TextureAtlasSprite[][] animatedSprites;
    //?} else {
    /*@Shadow @Final private RenderSection[] sections;
    *///?}

    @Override
    public TextureAtlasSprite[] polydium$sectionSprites(int section) {
        //? if >= 26.1 {
        return this.animatedSprites[section];
        //?} else {
        /*RenderSection render = this.sections[section];
        return render != null ? render.getAnimatedSprites() : null;
        *///?}
    }

    @Override
    public void polydium$forgetSprites() {
        SpriteSet.invalidate();
    }

    //? if >= 26.1 {
    @Inject(method = {"setSectionRenderState", "clearSectionRenderState"}, at = @At("TAIL"), require = 0)
    private void polydium$forgetSpritesOnChange(CallbackInfo ci) {
        this.polydium$forgetSprites();
    }
    //?}
}
