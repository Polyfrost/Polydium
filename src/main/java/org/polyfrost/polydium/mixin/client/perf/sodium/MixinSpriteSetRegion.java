package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
//? if < 26.1 {
/*import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
*///?}
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.perf.SpriteSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private TextureAtlasSprite[] polydium$sprites;

    @Override
    public TextureAtlasSprite[] polydium$uniqueSprites() {
        TextureAtlasSprite[] unique = this.polydium$sprites;
        if (unique == null) {
            ReferenceOpenHashSet<TextureAtlasSprite> set = new ReferenceOpenHashSet<>();
            //? if >= 26.1 {
            for (TextureAtlasSprite[] sprites : this.animatedSprites) {
                SpriteSet.add(set, sprites);
            }
            //?} else {
            /*for (RenderSection section : this.sections) {
                if (section != null) {
                    SpriteSet.add(set, section.getAnimatedSprites());
                }
            }
            *///?}
            this.polydium$sprites = unique = set.toArray(new TextureAtlasSprite[0]);
        }
        return unique;
    }

    @Override
    public void polydium$forgetSprites() {
        this.polydium$sprites = null;
        SpriteSet.invalidate();
    }

    //? if >= 26.1 {
    @Inject(method = {"setSectionRenderState", "clearSectionRenderState"}, at = @At("TAIL"), require = 0)
    private void polydium$forgetSpritesOnChange(CallbackInfo ci) {
        this.polydium$forgetSprites();
    }
    //?}
}
//?}
