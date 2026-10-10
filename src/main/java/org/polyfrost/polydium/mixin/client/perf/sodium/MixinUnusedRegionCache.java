package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
//? if >= 26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} else {
/*import it.unimi.dsi.fastutil.objects.ObjectArrayList;
*///?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(/*? if >= 26.2 {*/LevelExtractor/*?} else {*//*LevelRenderer*//*?}*/.class)
public abstract class MixinUnusedRegionCache {
    @Unique
    private static final RenderRegionCache POLYDIUM_UNUSED = new RenderRegionCache();

    //? if >= 26.2 {
    @Shadow
    @Final
    private LevelRenderer levelRenderer;
    //?} else {
    /*@Shadow
    @Final
    private ObjectArrayList<?> visibleSections;
    *///?}

    @WrapOperation(method = /*? if >= 26.2 {*/"extract"/*?} else {*//*"compileSections"*//*?}*/, at = @At(value = "NEW", target = "()Lnet/minecraft/client/renderer/chunk/RenderRegionCache;"))
    private RenderRegionCache polydium$cacheOnlyWhenUsed(Operation<RenderRegionCache> original) {
        if (this./*? if >= 26.2 {*/levelRenderer.visibleSections()/*?} else {*//*visibleSections*//*?}*/.isEmpty()) {
            return POLYDIUM_UNUSED;
        }
        return original.call();
    }
}
//?}
