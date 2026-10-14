package org.polyfrost.polydium.vulkan.compat;

public record HintsAndWorkarounds(boolean writeToBufferIsSlow, boolean anisotropyHasKnownIssues) {
}
