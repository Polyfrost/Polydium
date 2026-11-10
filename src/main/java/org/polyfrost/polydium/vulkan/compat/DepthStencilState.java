package org.polyfrost.polydium.vulkan.compat;

//? if < 26.1 {
/*public record DepthStencilState(CompareOp depthTest, boolean writeDepth, float depthBiasScaleFactor, float depthBiasConstant) {
  public static final DepthStencilState DEFAULT = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true);

  public DepthStencilState(final CompareOp depthTest, final boolean depthWrite) {
    this(depthTest, depthWrite, 0.0F, 0.0F);
  }
}
*///?}
