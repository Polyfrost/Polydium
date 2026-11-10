package org.polyfrost.polydium.vulkan.compat;

import com.mojang.blaze3d.systems.*;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.Collection;
import java.util.Optional;

public class GpuSurface implements AutoCloseable {
  private final GpuSurfaceBackend backend;
  private boolean hasImageAcquired = false;
  private boolean hasBlittedTexture = false;
  private Optional<GpuSurface.Configuration> currentConfiguration = Optional.empty();

  public GpuSurface(final GpuSurfaceBackend backend) {
    this.backend = backend;
  }

  @Override
  public void close() {
    if (this.hasImageAcquired) {
      throw new IllegalStateException("Cannot close a surface while it is acquired");
    } else {
      this.backend.close();
    }
  }

  public void configure(final GpuSurface.Configuration config) throws SurfaceException {
    if (this.hasImageAcquired) {
      throw new IllegalStateException("Cannot configure a surface while it is acquired");
    } else if (!this.supportedPresentModes().contains(config.presentMode())) {
      throw new SurfaceException("Surface does not support present mode " + config.presentMode() + " (supported: " + this.supportedPresentModes() + ")");
    } else {
      this.backend.configure(config);
      this.currentConfiguration = Optional.of(config);
    }
  }

  public Optional<GpuSurface.Configuration> currentConfiguration() {
    return this.currentConfiguration;
  }

  public Collection<GpuSurface.PresentMode> supportedPresentModes() {
    return this.backend.supportedPresentModes();
  }

  public boolean isSuboptimal() {
    return this.backend.isSuboptimal();
  }

  public boolean isAcquired() {
    return this.hasImageAcquired;
  }

  public void acquireNextTexture() throws SurfaceException {
    if (this.hasImageAcquired) {
      throw new IllegalStateException("Cannot acquire a surface while it is already acquired");
    } else if (this.currentConfiguration.isEmpty()) {
      throw new IllegalStateException("Cannot acquire an unconfigured surface");
    } else {
      this.backend.acquireNextTexture();
      this.hasImageAcquired = true;
      this.hasBlittedTexture = false;
    }
  }

  public void blitFromTexture(final CommandEncoderBackend commandEncoder, final GpuTextureView textureView) {
    if (commandEncoder.isInRenderPass()) {
      throw new IllegalStateException("Close the existing render pass before presenting with a command encoder");
    } else if (!textureView.texture().getFormat().hasColorAspect()) {
      throw new IllegalStateException("Cannot present a non-color texture!");
    } else if ((textureView.texture().usage() & 2) == 0) {
      throw new IllegalStateException("Color texture must have USAGE_COPY_SRC to presented to the screen");
    } else if (textureView.texture().getDepthOrLayers() > 1) {
      throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for presentation");
    } else if (!this.hasImageAcquired) {
      throw new IllegalStateException("Cannot present to an unacquired surface");
    } else if (this.hasBlittedTexture) {
      throw new IllegalStateException("Already blitted to this frame!");
    } else {
      this.backend.blitFromTexture(commandEncoder, textureView);
      this.hasBlittedTexture = true;
    }
  }

  public void present() {
    if (!this.hasImageAcquired) {
      throw new IllegalStateException("Cannot present to a surface if it isn't acquired");
    } else if (!this.hasBlittedTexture) {
      throw new IllegalStateException("Must blit to surface before presenting!");
    } else {
      this.backend.present();
      this.hasImageAcquired = false;
    }
  }

  public record Configuration(int width, int height, GpuSurface.PresentMode presentMode) {
  }

  public static enum PresentMode {
    IMMEDIATE,
    MAILBOX,
    FIFO,
    FIFO_RELAXED;

    private static final GpuSurface.PresentMode[] PRESENT_MODES_VSYNC = new GpuSurface.PresentMode[]{FIFO_RELAXED, FIFO};
    private static final GpuSurface.PresentMode[] PRESENT_MODES_NO_VSYNC = new GpuSurface.PresentMode[]{IMMEDIATE, MAILBOX, FIFO};

    private PresentMode() {
    }

    public static GpuSurface.PresentMode getSupportedVsyncMode(final Collection<GpuSurface.PresentMode> supportedModes, final boolean vsync) {
      GpuSurface.PresentMode[] preferred = vsync ? PRESENT_MODES_VSYNC : PRESENT_MODES_NO_VSYNC;

      for (GpuSurface.PresentMode mode : preferred) {
        if (supportedModes.contains(mode)) {
          return mode;
        }
      }

      throw new IllegalStateException("No supported presentation mode was found");
    }
  }
}
