package org.polyfrost.polydium.mixin.client.perf.sodium.v08;

//? if >= 26.1 && < 26.2 {
/*import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.perf.SpriteSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
public abstract class MixinSpriteSetRegion implements SpriteSet.Access {
    @Shadow @Final private RenderSection[] sections;

    @Override
    public TextureAtlasSprite[] polydium$sectionSprites(int section) {
        RenderSection render = this.sections[section];
        return render != null ? ((SpriteSet.SectionSprites) (Object) render).polydium$animatedSprites() : null;
    }

    @Override
    public void polydium$forgetSprites() {
        SpriteSet.invalidate();
    }
}
*///?}
