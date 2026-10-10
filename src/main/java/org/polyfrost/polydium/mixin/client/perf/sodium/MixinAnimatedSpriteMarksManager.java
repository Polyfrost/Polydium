package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.polyfrost.polydium.client.perf.SpriteMarkVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager", remap = false)
public abstract class MixinAnimatedSpriteMarksManager {
    @Shadow
    private SortedRenderLists renderLists;

    @Unique
    private SortedRenderLists polydium$markedLists;
    @Unique
    private long polydium$markedVersion = -1L;

    @WrapMethod(method = "tickVisibleRenders")
    private void polydium$skipRepeatedMarks(Operation<Void> original) {
        long version = SpriteMarkVersion.current();
        if (this.renderLists == this.polydium$markedLists
                && version == this.polydium$markedVersion) {
            return;
        }
        original.call();
        this.polydium$markedLists = this.renderLists;
        this.polydium$markedVersion = version;
    }
}
//?}
