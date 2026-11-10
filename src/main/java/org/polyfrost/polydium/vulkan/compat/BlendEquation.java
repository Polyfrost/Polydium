package org.polyfrost.polydium.vulkan.compat;

import org.polyfrost.polydium.vulkan.compat.BlendFactor;
import org.polyfrost.polydium.vulkan.compat.BlendOp;

public record BlendEquation(BlendFactor sourceFactor, BlendFactor destFactor, BlendOp op) {
}
