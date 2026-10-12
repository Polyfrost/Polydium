package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.polyfrost.polydium.client.perf.ItemBoundsCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStackRenderState.class)
public class MixinItemBoundsCache {
    //? if < 26.1 {
    /*@Unique
    private static final Matrix4f POLYDIUM_NO_LOCAL_TRANSFORM = new Matrix4f();

    *///?}
    @Shadow
    ItemDisplayContext displayContext;
    @Shadow
    private int activeLayerCount;
    @Shadow
    private AABB cachedModelBoundingBox;
    @Shadow
    private ItemStackRenderState.LayerRenderState[] layers;

    @WrapMethod(method = "getModelBoundingBox")
    private AABB polydium$cachedModelBounds(Operation<AABB> original) {
        if (this.cachedModelBoundingBox != null
            || this.activeLayerCount != 1
            || !RenderSystem.isOnRenderThread()
            || !ItemBoundsCache.isPlainLayer(this.layers[0])) {
            return original.call();
        }
        ItemBoundsLayerAccessor layer = (ItemBoundsLayerAccessor) this.layers[0];
        boolean leftHand = this.displayContext.leftHand();
        //? if >= 26.1 {
        Matrix4f localTransform = layer.polydium$localTransform();
        //?} else {
        /*Matrix4f localTransform = POLYDIUM_NO_LOCAL_TRANSFORM;
        *///?}
        AABB bounds = ItemBoundsCache.find(layer.polydium$extents(), layer.polydium$itemTransform(), leftHand, localTransform);
        if (bounds != null) {
            this.cachedModelBoundingBox = bounds;
            return bounds;
        }
        bounds = original.call();
        ItemBoundsCache.store(bounds, layer.polydium$extents(), layer.polydium$itemTransform(), leftHand, localTransform);
        return bounds;
    }
}
//?}
