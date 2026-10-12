package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
//? if >= 1.21.4 {
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
//?} else {
/*import net.minecraft.world.entity.decoration.ItemFrame;
*///?}
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFrameRenderer.class)
public abstract class MixinItemFrameRenderOffset {
    @Unique
    private static final Vec3[] polydium$OFFSETS = polydium$offsets();

    //? if >= 1.21.4 {
    @WrapMethod(method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;)Lnet/minecraft/world/phys/Vec3;")
    private Vec3 polydium$sharedRenderOffset(ItemFrameRenderState state, Operation<Vec3> original) {
        return polydium$OFFSETS[state.direction.ordinal()];
    }
    //?} else {
    /*@WrapMethod(method = "getRenderOffset(Lnet/minecraft/world/entity/decoration/ItemFrame;F)Lnet/minecraft/world/phys/Vec3;")
    private Vec3 polydium$sharedRenderOffset(ItemFrame frame, float partialTick, Operation<Vec3> original) {
        return polydium$OFFSETS[frame.getDirection().ordinal()];
    }
    *///?}

    @Unique
    private static Vec3[] polydium$offsets() {
        Direction[] directions = Direction.values();
        Vec3[] offsets = new Vec3[directions.length];
        for (Direction direction : directions) {
            offsets[direction.ordinal()] = new Vec3(direction.getStepX() * 0.3F, -0.25, direction.getStepZ() * 0.3F);
        }
        return offsets;
    }
}
//?}
