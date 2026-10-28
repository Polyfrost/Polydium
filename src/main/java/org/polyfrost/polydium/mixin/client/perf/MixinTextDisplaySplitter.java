package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.4 {
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
*///?}
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = DisplayRenderer.TextDisplayRenderer.class, priority = 999)
public abstract class MixinTextDisplaySplitter extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState/*? if >= 1.21.4 {*/, TextDisplayEntityRenderState/*?}*/> {
    @Unique
    private final Display.TextDisplay.LineSplitter polydium$splitter = this::splitLines;

    //? if < 1.21.4 {
    /*@Shadow
    @Final
    private Font font;
    *///?}

    protected MixinTextDisplaySplitter(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow
    private Display.TextDisplay.CachedInfo splitLines(Component input, int width) {
        throw new AssertionError();
    }

    //? if >= 1.21.4 {
    @Overwrite
    public void extractRenderState(Display.TextDisplay entity, TextDisplayEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.textRenderState = entity.textRenderState();
        state.cachedInfo = entity.cacheDisplay(this.polydium$splitter);
    }
    //?} else {
    /*@Overwrite
    public void renderInner(Display.TextDisplay entity, Display.TextDisplay.TextRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
        byte flags = renderState.flags();
        boolean seeThrough = (flags & 2) != 0;
        boolean useDefaultBackground = (flags & 4) != 0;
        boolean shadow = (flags & 1) != 0;
        Display.TextDisplay.Align align = Display.TextDisplay.getAlign(flags);
        byte textOpacity = (byte) renderState.textOpacity().get(partialTick);
        int backgroundColor;
        if (useDefaultBackground) {
            float backgroundAlpha = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            backgroundColor = (int) (backgroundAlpha * 255.0F) << 24;
        } else {
            backgroundColor = renderState.backgroundColor().get(partialTick);
        }

        float y = 0.0F;
        Matrix4f pose = poseStack.last().pose();
        pose.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
        pose.scale(-0.025F, -0.025F, -0.025F);
        Display.TextDisplay.CachedInfo cachedInfo = entity.cacheDisplay(this.polydium$splitter);
        int lineHeight = this.font.lineHeight + 1;
        int width = cachedInfo.width();
        int height = cachedInfo.lines().size() * lineHeight;
        pose.translate(1.0F - width / 2.0F, -height, 0.0F);
        if (backgroundColor != 0) {
            VertexConsumer background = bufferSource.getBuffer(seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
            background.addVertex(pose, -1.0F, -1.0F, 0.0F).setColor(backgroundColor).setLight(light);
            background.addVertex(pose, -1.0F, (float) height, 0.0F).setColor(backgroundColor).setLight(light);
            background.addVertex(pose, (float) width, (float) height, 0.0F).setColor(backgroundColor).setLight(light);
            background.addVertex(pose, (float) width, -1.0F, 0.0F).setColor(backgroundColor).setLight(light);
        }

        for (Display.TextDisplay.CachedLine line : cachedInfo.lines()) {
            float x = align == Display.TextDisplay.Align.LEFT ? 0.0F
                : align == Display.TextDisplay.Align.RIGHT ? width - line.width()
                : width / 2.0F - line.width() / 2.0F;
            this.font.drawInBatch(line.contents(), x, y, textOpacity << 24 | 16777215, shadow, pose, bufferSource, seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET, 0, light);
            y += lineHeight;
        }
    }
    *///?}
}
