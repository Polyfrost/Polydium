package org.polyfrost.polydium.vulkan.compat;

import com.mojang.blaze3d.textures.GpuTextureView;

public record RenderArea(int x, int y, int width, int height) {
  public boolean fillsTexture(final GpuTextureView texture) {
    return this.x == 0 && this.y == 0 && this.width == texture.getWidth(0) && this.height == texture.getHeight(0);
  }
}
