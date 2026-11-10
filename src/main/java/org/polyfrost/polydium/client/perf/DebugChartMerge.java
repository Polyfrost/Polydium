package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//?} else {
/*import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DebugChartMerge {
    private static final ArrayList<ColoredRectangleRenderState> HELD = new ArrayList<>();
    private static GuiRenderState target;
    private static boolean merging;
    private static boolean submitting;

    private DebugChartMerge() {
    }

    public static void begin() {
        flush();
        merging = true;
    }

    public static void end() {
        try {
            flush();
        } finally {
            merging = false;
        }
    }

    public static boolean hold(GuiRenderState state, GuiElementRenderState element) {
        if (!merging || submitting) {
            return false;
        }
        if (!(element instanceof ColoredRectangleRenderState rectangle) || rectangle.bounds() == null) {
            flush();
            return false;
        }
        if (!HELD.isEmpty() && (target != state || !mergeable(HELD.get(0), rectangle))) {
            flush();
        }
        target = state;
        HELD.add(rectangle);
        return true;
    }

    public static void flush() {
        if (submitting || HELD.isEmpty()) {
            return;
        }
        submitting = true;
        try {
            GuiElementRenderState element = HELD.size() == 1 ? HELD.get(0) : new Merged(List.copyOf(HELD));
            //? if >= 26.1 {
            target.addGuiElement(element);
            //?} else {
            /*target.submitGuiElement(element);
            *///?}
        } finally {
            HELD.clear();
            target = null;
            submitting = false;
        }
    }

    private static boolean mergeable(ColoredRectangleRenderState first, ColoredRectangleRenderState next) {
        return first.pipeline() == next.pipeline()
                && Objects.equals(first.textureSetup(), next.textureSetup())
                && Objects.equals(first.scissorArea(), next.scissorArea());
    }

    private static final class Merged implements GuiElementRenderState {
        private final List<ColoredRectangleRenderState> rectangles;
        private final ScreenRectangle bounds;

        private Merged(List<ColoredRectangleRenderState> rectangles) {
            this.rectangles = rectangles;
            int left = Integer.MAX_VALUE;
            int top = Integer.MAX_VALUE;
            int right = Integer.MIN_VALUE;
            int bottom = Integer.MIN_VALUE;
            for (ColoredRectangleRenderState rectangle : rectangles) {
                ScreenRectangle bounds = rectangle.bounds();
                left = Math.min(left, bounds.left());
                top = Math.min(top, bounds.top());
                right = Math.max(right, bounds.right());
                bottom = Math.max(bottom, bounds.bottom());
            }
            this.bounds = new ScreenRectangle(left, top, right - left, bottom - top);
        }

        //? if >= 1.21.10 {
        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (ColoredRectangleRenderState rectangle : this.rectangles) {
                rectangle.buildVertices(consumer);
            }
        }
        //?} else {
        /*@Override
        public void buildVertices(VertexConsumer consumer, float z) {
            for (ColoredRectangleRenderState rectangle : this.rectangles) {
                rectangle.buildVertices(consumer, z);
            }
        }
        *///?}

        @Override
        public RenderPipeline pipeline() {
            return this.rectangles.get(0).pipeline();
        }

        @Override
        public TextureSetup textureSetup() {
            return this.rectangles.get(0).textureSetup();
        }

        @Override
        public ScreenRectangle scissorArea() {
            return this.rectangles.get(0).scissorArea();
        }

        @Override
        public ScreenRectangle bounds() {
            return this.bounds;
        }
    }
}
//?}
