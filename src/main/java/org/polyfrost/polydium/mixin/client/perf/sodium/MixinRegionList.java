package org.polyfrost.polydium.mixin.client.perf.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import org.polyfrost.polydium.client.perf.SwapRemoveIterator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager", remap = false)
public abstract class MixinRegionList {
    @Shadow @Final private Long2ReferenceOpenHashMap<RenderRegion> regions;

    @Unique
    private final SwapRemoveIterator<RenderRegion> polydium$regionList =
        new SwapRemoveIterator<>(region -> this.regions.remove(RenderRegion.key(region.getX(), region.getY(), region.getZ())));

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ReferenceCollection;iterator()Lit/unimi/dsi/fastutil/objects/ObjectIterator;"))
    private ObjectIterator<RenderRegion> polydium$iterateList(ReferenceCollection<RenderRegion> values, Operation<ObjectIterator<RenderRegion>> original) {
        SwapRemoveIterator<RenderRegion> iterator = this.polydium$regionList;
        // regions are only ever removed through this iterator or delete(), so a size mismatch means new regions were added
        if (iterator.list.size() != this.regions.size()) {
            iterator.list.clear();
            original.call(values).forEachRemaining(iterator.list::add);
        }
        return iterator.restart();
    }

    @Inject(method = "delete", at = @At("TAIL"))
    private void polydium$forgetRegions(CallbackInfo ci) {
        this.polydium$regionList.list.clear();
    }
}
