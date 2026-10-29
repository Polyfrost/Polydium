package org.polyfrost.polydium.mixin.client.perf;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.polyfrost.polydium.client.perf.NameTagReuse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class MixinEntityNameTagMemo implements NameTagReuse.Memo {
    @Unique
    private Component polydium$nameTag;

    @Unique
    private Component polydium$scoreText;

    @Override
    public Component polydium$reuseNameTag(Component nameTag) {
        if (NameTagReuse.matches(this.polydium$nameTag, nameTag)) {
            return this.polydium$nameTag;
        }
        this.polydium$nameTag = NameTagReuse.remembered(nameTag);
        return this.polydium$nameTag != null ? this.polydium$nameTag : nameTag;
    }

    @Override
    public Component polydium$reuseScoreText(Component scoreText) {
        if (NameTagReuse.matches(this.polydium$scoreText, scoreText)) {
            return this.polydium$scoreText;
        }
        this.polydium$scoreText = NameTagReuse.remembered(scoreText);
        return this.polydium$scoreText != null ? this.polydium$scoreText : scoreText;
    }
}
