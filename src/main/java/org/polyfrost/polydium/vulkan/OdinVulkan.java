package org.polyfrost.polydium.vulkan;

//? if >= 26.1 {
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public final class OdinVulkan {
  private static final VertexFormat FORMAT = VertexFormat.builder()
    .add("Position", VertexFormatElement.POSITION)
    .add("Color", VertexFormatElement.COLOR)
    .add("UV0", VertexFormatElement.UV0)
    .add("UV1", VertexFormatElement.UV1)
    .add("UV2", VertexFormatElement.UV2)
    .add("LineWidth", VertexFormatElement.LINE_WIDTH)
    .build();
  private static final RenderPipeline ROUND_RECT = pipeline("round_rect", false);
  private static final RenderPipeline ROUND_RECT_TEXTURED = pipeline("round_rect_textured", true);
  private static final RenderPipeline ROUND_RECT_SHADOW = pipeline("round_rect_shadow", false);
  private static final float AA_PADDING = 2.0F;
  private static final float TEXT_SCALE = 1.0F / 12.0F;
  private static final float TEXT_SHIFT = 0.2F;
  private static final MethodHandle GUI_RENDER_STATE;
  private static final MethodHandle SCISSOR_STACK;
  private static final MethodHandle SCISSOR_PEEK;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(GuiGraphicsExtractor.class, MethodHandles.lookup());
      Class<?> scissorStack = Class.forName(GuiGraphicsExtractor.class.getName() + "$ScissorStack");
      GUI_RENDER_STATE = lookup.findGetter(GuiGraphicsExtractor.class, "guiRenderState", GuiRenderState.class);
      SCISSOR_STACK = lookup.findGetter(GuiGraphicsExtractor.class, "scissorStack", scissorStack).asType(MethodType.methodType(Object.class, GuiGraphicsExtractor.class));
      SCISSOR_PEEK = lookup.findVirtual(scissorStack, "peek", MethodType.methodType(ScreenRectangle.class)).asType(MethodType.methodType(ScreenRectangle.class, Object.class));
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static final Map<Integer, DynamicTexture> IMAGES = new HashMap<>();
  private static MethodHandle invoke;
  private static int nextImage = 1;
  private static @Nullable GuiGraphicsExtractor graphics;
  private static int scissors;

  private OdinVulkan() {
  }

  private static RenderPipeline pipeline(String name, boolean textured) {
    RenderPipeline.Builder builder = RenderPipeline.builder()
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withLocation(Identifier.fromNamespaceAndPath("polydium", "pipeline/odin_" + name))
      .withVertexShader(Identifier.fromNamespaceAndPath("polydium", "core/odin_round_rect"))
      .withFragmentShader(Identifier.fromNamespaceAndPath("polydium", "core/odin_" + name))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withVertexFormat(FORMAT, VertexFormat.Mode.QUADS);
    if (textured) builder.withSampler("Sampler0");
    return builder.build();
  }

  public static void draw(GuiGraphicsExtractor context, int x, int y, int width, int height, Object renderContent) throws Throwable {
    if (invoke == null) {
      Class<?> function0 = Class.forName("kotlin.jvm.functions.Function0", false, renderContent.getClass().getClassLoader());
      invoke = MethodHandles.publicLookup().findVirtual(function0, "invoke", MethodType.methodType(Object.class))
        .asType(MethodType.methodType(Object.class, Object.class));
    }

    var window = Minecraft.getInstance().getWindow();
    float dpr = window.getScreenWidth() == 0 ? 1.0F : (float) (window.getWidth() / window.getScreenWidth());
    float scale = dpr / window.getGuiScale();
    Matrix3x2fStack pose = context.pose();
    pose.pushMatrix();
    pose.translate(x, y);
    pose.scale(scale, scale);
    graphics = context;
    try {
      Object unused = invoke.invokeExact(renderContent);
    } finally {
      for (; scissors > 0; scissors--) context.disableScissor();
      graphics = null;
      pose.popMatrix();
    }
  }

  public static void push() {
    if (graphics != null) graphics.pose().pushMatrix();
  }

  public static void pop() {
    if (graphics != null) graphics.pose().popMatrix();
  }

  public static void scale(float x, float y) {
    if (graphics != null) graphics.pose().scale(x, y);
  }

  public static void translate(float x, float y) {
    if (graphics != null) graphics.pose().translate(x, y);
  }

  public static void rotate(float amount) {
    if (graphics != null) graphics.pose().rotate(amount);
  }

  public static void pushScissor(float x, float y, float w, float h) {
    if (graphics == null) return;
    graphics.enableScissor((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(x + w), (int) Math.ceil(y + h));
    scissors++;
  }

  public static void popScissor() {
    if (graphics == null || scissors == 0) return;
    graphics.disableScissor();
    scissors--;
  }

  public static void line(float x1, float y1, float x2, float y2, float thickness, int color) {
    if (graphics == null) return;
    Matrix3x2fStack pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(x1, y1);
    pose.rotate((float) Math.atan2(y2 - y1, x2 - x1));
    fill(0.0F, -thickness / 2.0F, (float) Math.hypot(x2 - x1, y2 - y1), thickness / 2.0F, color, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    pose.popMatrix();
  }

  public static void drawHalfRoundedRect(float x, float y, float w, float h, int color, float radius, boolean roundTop) {
    float top = roundTop ? radius : 0.0F, bottom = roundTop ? 0.0F : radius;
    fill(x, y, x + w, y + h, color, top, top, bottom, bottom, 0.0F);
  }

  public static void rect(float x, float y, float w, float h, int color, float radius) {
    fill(x, y, x + w, y + h, color, radius, radius, radius, radius, 0.0F);
  }

  public static void rect(float x, float y, float w, float h, int color) {
    fill(x, y, x + w, y + h, color, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
  }

  public static void hollowRect(float x, float y, float w, float h, float thickness, int color, float radius) {
    float half = thickness / 2.0F, r = radius + half;
    fill(x - half, y - half, x + w + half, y + h + half, color, r, r, r, r, thickness);
  }

  public static void gradientRect(float x, float y, float w, float h, int color1, int color2, Object gradient, float radius) {
    boolean leftToRight = ((Enum<?>) gradient).ordinal() == 0;
    emit(x, y, x + w, y + h, color1, leftToRight ? color2 : color1, color2, leftToRight ? color1 : color2,
      radius, radius, radius, radius, 0.0F, AA_PADDING, ROUND_RECT, TextureSetup.noTexture());
  }

  public static void dropShadow(float x, float y, float width, float height, float blur, float spread, float radius) {
    float r = radius + spread;
    emit(x - spread, y - spread, x + width + spread, y + height + spread, 0x7D000000, 0x7D000000, 0x7D000000, 0x7D000000,
      r, r, r, r, blur, AA_PADDING + blur, ROUND_RECT_SHADOW, TextureSetup.noTexture());
  }

  public static void circle(float x, float y, float radius, int color) {
    fill(x - radius, y - radius, x + radius, y + radius, color, radius, radius, radius, radius, 0.0F);
  }

  public static void text(String text, float x, float y, float size, int color, Object font) {
    if (graphics == null) return;
    Matrix3x2fStack pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(x, y + size * TEXT_SHIFT);
    pose.scale(size * TEXT_SCALE, size * TEXT_SCALE);
    graphics.text(Minecraft.getInstance().font, text, 0, 0, color, false);
    pose.popMatrix();
  }

  public static void textShadow(String text, float x, float y, float size, int color, Object font) {
    text(text, (float) Math.rint(x + 2.0F), (float) Math.rint(y + 2.0F), size, 0xFF000000, font);
    text(text, (float) Math.rint(x), (float) Math.rint(y), size, color, font);
  }

  public static float textWidth(String text, float size, Object font) {
    return Minecraft.getInstance().font.width(text) * size * TEXT_SCALE;
  }

  public static void drawWrappedString(String text, float x, float y, float w, float size, int color, Object font, float lineHeight) {
    if (graphics == null) return;
    Font mcFont = Minecraft.getInstance().font;
    Matrix3x2fStack pose = graphics.pose();
    List<FormattedCharSequence> lines = mcFont.split(FormattedText.of(text), (int) (w / (size * TEXT_SCALE)));
    for (int i = 0; i < lines.size(); i++) {
      pose.pushMatrix();
      pose.translate(x, y + size * (TEXT_SHIFT + i * lineHeight));
      pose.scale(size * TEXT_SCALE, size * TEXT_SCALE);
      graphics.text(mcFont, lines.get(i), 0, 0, color, false);
      pose.popMatrix();
    }
  }

  public static float[] wrappedTextBounds(String text, float w, float size, Object font, float lineHeight) {
    Font mcFont = Minecraft.getInstance().font;
    List<FormattedCharSequence> lines = mcFont.split(FormattedText.of(text), (int) (w / (size * TEXT_SCALE)));
    int widest = 0;
    for (FormattedCharSequence line : lines) widest = Math.max(widest, mcFont.width(line));
    return new float[]{0.0F, 0.0F, widest * size * TEXT_SCALE, lines.size() * size * lineHeight};
  }

  public static void image(int image, float x, float y, float w, float h, float radius) {
    DynamicTexture texture = IMAGES.get(image);
    if (texture == null) return;
    emit(x, y, x + w, y + h, -1, -1, -1, -1, radius, radius, radius, radius, 0.0F, AA_PADDING, ROUND_RECT_TEXTURED,
      TextureSetup.singleTexture(texture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
  }

  public static void image(int image, float x, float y, float w, float h) {
    image(image, x, y, w, h, 0.0F);
  }

  public static long nvgCreate(int flags) throws Throwable {
    return VulkanActive.active ? 0L : (long) nanoVG("NanoVGGL3", "nvgCreate", MethodType.methodType(long.class, int.class)).invokeExact(flags);
  }

  public static int nvgCreateImageRGBA(long ctx, int w, int h, int imageFlags, ByteBuffer data) throws Throwable {
    if (!VulkanActive.active)
      return (int) nanoVG("NanoVG", "nvgCreateImageRGBA", MethodType.methodType(int.class, long.class, int.class, int.class, int.class, ByteBuffer.class))
        .invokeExact(ctx, w, h, imageFlags, data);
    NativeImage image = new NativeImage(w, h, false);
    MemoryUtil.memCopy(MemoryUtil.memAddress(data), image.getPointer(), (long) w * h * 4);
    int id = nextImage++;
    IMAGES.put(id, new DynamicTexture(() -> "odin_image_" + id, image));
    return id;
  }

  public static void nvgDeleteImage(long ctx, int image) throws Throwable {
    if (!VulkanActive.active) {
      nanoVG("NanoVG", "nvgDeleteImage", MethodType.methodType(void.class, long.class, int.class)).invokeExact(ctx, image);
      return;
    }
    DynamicTexture texture = IMAGES.remove(image);
    if (texture != null) texture.close();
  }

  private static MethodHandle nanoVG(String owner, String name, MethodType type) throws ReflectiveOperationException {
    return MethodHandles.publicLookup().findStatic(Class.forName("org.lwjgl.nanovg." + owner, true, OdinVulkan.class.getClassLoader()), name, type);
  }

  private static void fill(float x0, float y0, float x1, float y1, int color, float tl, float tr, float br, float bl, float edge) {
    emit(x0, y0, x1, y1, color, color, color, color, tl, tr, br, bl, edge, AA_PADDING, ROUND_RECT, TextureSetup.noTexture());
  }

  private static void emit(float x0, float y0, float x1, float y1, int tlColor, int trColor, int brColor, int blColor,
                           float tl, float tr, float br, float bl, float edge, float padding, RenderPipeline pipeline, TextureSetup textureSetup) {
    if (graphics == null || ((tlColor | trColor | brColor | blColor) & 0xFF000000) == 0) return;
    float left = Math.min(x0, x1), top = Math.min(y0, y1), right = Math.max(x0, x1), bottom = Math.max(y0, y1);
    float halfWidth = (right - left) * 0.5F, halfHeight = (bottom - top) * 0.5F;
    if (halfWidth <= 0.0F || halfHeight <= 0.0F) return;

    Matrix3x2f pose = new Matrix3x2f(graphics.pose());
    int quadX0 = (int) Math.floor(left - padding), quadY0 = (int) Math.floor(top - padding);
    int quadX1 = (int) Math.ceil(right + padding), quadY1 = (int) Math.ceil(bottom + padding);
    ScreenRectangle area = new ScreenRectangle(quadX0, quadY0, quadX1 - quadX0, quadY1 - quadY0).transformMaxBounds(pose);
    try {
      ScreenRectangle scissor = (ScreenRectangle) SCISSOR_PEEK.invokeExact(SCISSOR_STACK.invokeExact(graphics));
      GuiRenderState state = (GuiRenderState) GUI_RENDER_STATE.invokeExact(graphics);
      state.addGuiElement(new Element(pose, quadX0, quadY0, quadX1, quadY1, (left + right) * 0.5F, (top + bottom) * 0.5F,
        tlColor, trColor, brColor, blColor, fixedPoint(halfWidth), fixedPoint(halfHeight),
        packRadius(tl) | packRadius(tr) << 8, packRadius(br) | packRadius(bl) << 8, edge, pipeline, textureSetup,
        scissor, scissor != null ? scissor.intersection(area) : area));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private static int fixedPoint(float value) {
    return Math.clamp(Math.round(value * 8.0F), -32768, 32767);
  }

  private static int packRadius(float radius) {
    return Math.clamp(Math.round(Math.clamp(radius, 0.0F, 63.75F) * 4.0F), 0, 255);
  }

  private record Element(Matrix3x2f pose, float x0, float y0, float x1, float y1, float centerX, float centerY,
                         int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor,
                         int packedHalfWidth, int packedHalfHeight, int packedRadiiTop, int packedRadiiBottom, float edgeWidth,
                         RenderPipeline pipeline, TextureSetup textureSetup, @Nullable ScreenRectangle scissorArea,
                         @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer consumer) {
      vertex(consumer, x0, y0, topLeftColor);
      vertex(consumer, x0, y1, bottomLeftColor);
      vertex(consumer, x1, y1, bottomRightColor);
      vertex(consumer, x1, y0, topRightColor);
    }

    private void vertex(VertexConsumer consumer, float x, float y, int color) {
      consumer.addVertexWith2DPose(pose, x, y)
        .setColor(color)
        .setUv(x - centerX, y - centerY)
        .setUv1(packedHalfWidth, packedHalfHeight)
        .setUv2(packedRadiiTop, packedRadiiBottom)
        .setLineWidth(edgeWidth);
    }
  }
}
//?}
