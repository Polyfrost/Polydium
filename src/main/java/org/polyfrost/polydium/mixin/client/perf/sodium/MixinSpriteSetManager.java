package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.polyfrost.polydium.client.perf.IrisShaders;
import org.polyfrost.polydium.client.perf.SpriteSet;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Iterator;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager", remap = false)
public abstract class MixinSpriteSetManager {
    @Shadow
    private SortedRenderLists renderLists;

    @Unique
    private final ReferenceOpenHashSet<TextureAtlasSprite> polydium$spriteSet = new ReferenceOpenHashSet<>();
    @Unique
    private TextureAtlasSprite[] polydium$sprites;
    @Unique
    private SortedRenderLists polydium$setLists;
    @Unique
    private int polydium$setGeneration;
    @Unique
    private boolean polydium$setUnsupported;

    @ModifyExpressionValue(method = "tickVisibleRenders", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;iterator()Ljava/util/Iterator;"), require = 0)
    private Iterator<ChunkRenderList> polydium$markSpriteSet(Iterator<ChunkRenderList> lists) {
        if (this.polydium$setUnsupported || IrisShaders.packInUse()) {
            this.polydium$setLists = null;
            return lists;
        }
        SortedRenderLists current = this.renderLists;
        int generation = SpriteSet.generation();
        if (current != this.polydium$setLists || generation != this.polydium$setGeneration) {
            ReferenceOpenHashSet<TextureAtlasSprite> set = this.polydium$spriteSet;
            set.clear();
            while (lists.hasNext()) {
                ChunkRenderList list = lists.next();
                if (list.getSectionsWithSpritesCount() == 0) {
                    continue;
                }
                if (!((Object) list.getRegion() instanceof SpriteSet.Access region)) {
                    this.polydium$setUnsupported = true;
                    LoggerFactory.getLogger("polydium/perf").debug("SpriteSet stands down: region sprite cache is missing");
                    return current.iterator();
                }
                SpriteSet.add(set, region.polydium$uniqueSprites());
            }
            this.polydium$sprites = set.toArray(new TextureAtlasSprite[0]);
            this.polydium$setLists = current;
            this.polydium$setGeneration = generation;
        }
        for (TextureAtlasSprite sprite : this.polydium$sprites) {
            SpriteUtil.INSTANCE.markSpriteActive(sprite);
        }
        return Collections.emptyIterator();
    }
}
//?}
