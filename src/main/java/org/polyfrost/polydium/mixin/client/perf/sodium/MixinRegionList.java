package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
//? if >= 26.2 {
import net.caffeinemc.mods.sodium.client.gpu.arena.ArenaAggregator;
import net.caffeinemc.mods.sodium.client.gpu.arena.staging.StagingBuffer;
//?} else {
/*import net.caffeinemc.mods.sodium.client.gl.arena.staging.StagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
*///?}
//? if >= 26.1 && < 26.2 {
/*import net.caffeinemc.mods.sodium.client.gl.arena.ArenaAggregator;
*///?}
//? if >= 26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.IntPool;
import net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager;
//?}
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager", remap = false)
public abstract class MixinRegionList {
    @Shadow @Final private Long2ReferenceOpenHashMap<RenderRegion> regions;
    @Shadow @Final private StagingBuffer stagingBuffer;
    //? if >= 26.1 {
    @Shadow @Final private IntPool freeIds;
    @Shadow @Final private ArenaAggregator arenaAggregator;
    //?}

    @Unique
    private final ArrayList<RenderRegion> polydium$regionList = new ArrayList<>();
    @Unique
    private boolean polydium$listStale;

    @WrapMethod(method = "update")
    //? if >= 26.1 {
    private void polydium$updateFromList(UniformBufferManager uniforms, Operation<Void> original) {
    //?} else {
    /*private void polydium$updateFromList(Operation<Void> original) {
    *///?}
        ArrayList<RenderRegion> list = this.polydium$regionList;
        if (this.polydium$listStale || list.size() != this.regions.size()) {
            this.polydium$listStale = false;
            list.clear();
            list.addAll(this.regions.values());
        }
        this.stagingBuffer.flip();
        //? if >= 26.2 {
        this.arenaAggregator.update();
        //?} else {
        /*CommandList commandList = RenderDevice.INSTANCE.createCommandList();
        *///?}
        //? if >= 26.1 && < 26.2 {
        /*this.arenaAggregator.update(commandList);
        *///?}
        int index = 0;
        while (index < list.size()) {
            RenderRegion region = list.get(index);
            //? if >= 26.2 {
            region.update();
            //?} else {
            /*region.update(commandList);
            *///?}
            if (!region.isEmpty()) {
                index++;
                continue;
            }
            //? if >= 26.2 {
            region.delete();
            //?} else {
            /*region.delete(commandList);
            *///?}
            //? if >= 26.1 {
            if (region.getId() != -1) {
                this.freeIds.release(region.getId());
                uniforms.clearRegionTimes(region.getId());
            }
            //?}
            this.regions.remove(RenderRegion.key(region.getX(), region.getY(), region.getZ()));
            RenderRegion last = list.remove(list.size() - 1);
            if (index < list.size()) {
                list.set(index, last);
            }
        }
    }

    @Inject(method = "delete", at = @At("TAIL"))
    private void polydium$forgetRegions(CallbackInfo ci) {
        this.polydium$regionList.clear();
    }
}
//?}
