package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PreparedRenderType.class)
public class MixinPreparedRenderTypeHash {
    @Unique
    private int polydium$hash;
    @Unique
    private boolean polydium$hashed;

    @WrapMethod(method = "hashCode")
    private int polydium$hashOnce(Operation<Integer> original) {
        if (!this.polydium$hashed) {
            this.polydium$hash = original.call();
            this.polydium$hashed = true;
        }
        return this.polydium$hash;
    }
}
//?}
