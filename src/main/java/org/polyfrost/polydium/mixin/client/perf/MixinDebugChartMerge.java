package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import org.polyfrost.polydium.client.perf.DebugChartMerge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractDebugChart.class)
public class MixinDebugChartMerge {
    //? if >= 26.3 {
    @WrapMethod(method = "extractRenderState")
    private void polydium$mergeRectangles(GuiGraphicsExtractor graphics, int x, int width, int bottom, Operation<Void> original) {
        DebugChartMerge.begin();
        try {
            original.call(graphics, x, width, bottom);
        } finally {
            DebugChartMerge.end();
        }
    }
    //?} else {
    /*@WrapMethod(method = /^? if >= 26.1 {^/"extractRenderState"/^?} else {^//^"drawChart"^//^?}^/)
    private void polydium$mergeRectangles(/^? if >= 26.1 {^/GuiGraphicsExtractor/^?} else {^//^GuiGraphics^//^?}^/ graphics, int x, int width, Operation<Void> original) {
        DebugChartMerge.begin();
        try {
            original.call(graphics, x, width);
        } finally {
            DebugChartMerge.end();
        }
    }
    *///?}
}
//?}
