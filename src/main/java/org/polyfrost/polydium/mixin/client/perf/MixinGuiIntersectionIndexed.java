package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.navigation.ScreenRectangle;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.ScreenArea;
//?} else {
/*import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.ScreenArea;
*///?}
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.RandomAccess;

@Mixin(GuiRenderState.class)
public class MixinGuiIntersectionIndexed {
    @WrapMethod(method = "hasIntersection")
    private boolean polydium$indexedIntersection(ScreenRectangle rectangle, List<? extends ScreenArea> areas, Operation<Boolean> original) {
        if (!(areas instanceof RandomAccess)) {
            return original.call(rectangle, areas);
        }
        for (int i = 0; i < areas.size(); i++) {
            ScreenRectangle bounds = areas.get(i).bounds();
            if (bounds != null && bounds.intersects(rectangle)) {
                return true;
            }
        }
        return false;
    }
}
//?}
