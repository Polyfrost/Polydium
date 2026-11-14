package org.polyfrost.polydium.mixin.client.perf.flashback;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = {"com.moulberry.flashback.playback.ReplayServer", "com.moulberry.flashback.playback.ReplayGamePacketHandler"}, remap = false)
public abstract class MixinReplayEntityTickets {
    @Unique
    private static final int polydium$MAX_PLAYER_DISTANCE = 64;

    @WrapOperation(method = {"runUpdates", "flushPendingEntities"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V"),
            remap = false, require = 0)
    private void polydium$skipFarEntityTicket(ServerChunkCache chunkCache, TicketType type, ChunkPos pos, int radius, Operation<Void> original) {
        for (Player player : chunkCache.getLevel().players()) {
            if (player.chunkPosition().getChessboardDistance(pos) <= polydium$MAX_PLAYER_DISTANCE) {
                original.call(chunkCache, type, pos, radius);
                return;
            }
        }
    }
}
//?}
