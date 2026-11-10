package org.polyfrost.polydium.vulkan.compat;

//? if < 26.1 {
/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public interface CommandEncoderBackend extends CommandEncoder {
  @Override
  RenderPassBackend createRenderPass(Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor);

  @Override
  RenderPassBackend createRenderPass(
    Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor, @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth
  );

  boolean isInRenderPass();

  @Override
  default GpuBuffer.MappedView mapBuffer(final GpuBuffer buffer, final boolean read, final boolean write) {
    return this.mapBuffer(buffer.slice(), read, write);
  }

  @Override
  default void writeToTexture(final GpuTexture destination, final NativeImage source) {
    this.writeToTexture(destination, source, 0, 0, 0, 0, destination.getWidth(0), destination.getHeight(0), 0, 0);
  }
}
*///?}
