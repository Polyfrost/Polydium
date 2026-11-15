package org.polyfrost.polydium.client.perf.gpuprofile;

public final class GpuProfileFlag {
    public static final boolean ENABLED = Boolean.getBoolean("polydium.gpuProfiler");

    private GpuProfileFlag() {
    }
}
