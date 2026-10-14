package org.polyfrost.polydium.mixin.client.perf.sodium.v08;

//? if >= 26.1 && < 26.2 {
/*import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.perf.SpriteSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion", remap = false)
public abstract class MixinSpriteSetRegion implements SpriteSet.Access {
    @Shadow @Final private RenderSection[] sections;

    @Unique
    private TextureAtlasSprite[] polydium$sprites;

    @Override
    public TextureAtlasSprite[] polydium$uniqueSprites() {
        TextureAtlasSprite[] unique = this.polydium$sprites;
        if (unique == null) {
            ReferenceOpenHashSet<TextureAtlasSprite> set = new ReferenceOpenHashSet<>();
            for (RenderSection section : this.sections) {
                if (section != null) {
                    SpriteSet.add(set, ((SpriteSet.SectionSprites) (Object) section).polydium$animatedSprites());
                }
            }
            this.polydium$sprites = unique = set.toArray(new TextureAtlasSprite[0]);
        }
        return unique;
    }

    @Override
    public void polydium$forgetSprites() {
        this.polydium$sprites = null;
        SpriteSet.invalidate();
    }
}
*///?}
