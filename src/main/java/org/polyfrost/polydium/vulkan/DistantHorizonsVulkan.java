package org.polyfrost.polydium.vulkan;

import net.minecraft.resources.Identifier;

public final class DistantHorizonsVulkan {
  private DistantHorizonsVulkan() {
  }

  static String shader(Identifier id, String source) {
    if (source == null || !id.getNamespace().equals("distanthorizons"))
      return source;

    source = source.replace("dFdxFine(", "dFdx(").replace("dFdyFine(", "dFdy(");
    if (!VulkanActive.zZeroToOne())
      return source;
    return source.replace("ndc.xyz = ndc.xyz * 2.0 - 1.0;", "ndc.xy = ndc.xy * 2.0 - 1.0;")
      .replaceAll("vec4\\((\\w+) \\* 2\\.0 - 1\\.0, 1\\.0\\)", "vec4($1.xy * 2.0 - 1.0, $1.z, 1.0)");
  }

  public static float m22(float nearClip, float farClip) {
    return farClip / (nearClip - farClip);
  }

  public static float m23(float nearClip, float farClip) {
    return farClip * nearClip / (nearClip - farClip);
  }
}
