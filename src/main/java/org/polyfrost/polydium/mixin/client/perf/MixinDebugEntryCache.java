package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.10 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.polyfrost.polydium.client.perf.DebugEntryCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugEntryCache {
    @WrapOperation(
            method = /*? if >= 26.1 {*/"extractRenderState"/*?} else {*//*"render"*//*?}*/,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntry;display(Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/chunk/LevelChunk;)V")
    )
    private void polydium$cachedEntry(DebugScreenEntry entry, DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk, Operation<Void> original) {
        if (!DebugEntryCache.cacheable(entry)) {
            original.call(entry, displayer, level, clientChunk, serverChunk);
            return;
        }
        long now = System.nanoTime();
        if (!DebugEntryCache.replay(entry, displayer, level, clientChunk, serverChunk, now)) {
            DebugScreenDisplayer recorder = DebugEntryCache.recorder(entry, displayer, level, clientChunk, serverChunk, now);
            original.call(entry, recorder, level, clientChunk, serverChunk);
            DebugEntryCache.finish(recorder);
        }
    }
}
//?}
