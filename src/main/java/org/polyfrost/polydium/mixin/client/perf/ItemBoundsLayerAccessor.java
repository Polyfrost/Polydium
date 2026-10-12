package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import java.util.function.Supplier;
import net.minecraft.client.renderer.item.ItemStackRenderState;
//? if >= 26.1 {
import net.minecraft.client.resources.model.cuboid.ItemTransform;
//?} else {
/*import net.minecraft.client.renderer.block.model.ItemTransform;
*///?}
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemBoundsLayerAccessor {
    @Accessor("extents")
    Supplier<Vector3fc[]> polydium$extents();

    //? if >= 26.1 {
    @Accessor("itemTransform")
    ItemTransform polydium$itemTransform();

    @Accessor("localTransform")
    Matrix4f polydium$localTransform();
    //?} else {
    /*@Accessor("transform")
    ItemTransform polydium$itemTransform();
    *///?}
}
//?}
