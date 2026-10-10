package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public interface PipelineUniforms {
    String[] polydium$uniformNames();

    int[] polydium$uniformSlots();

    long polydium$validatedPass();

    void polydium$validatedPass(long pass);
}
//?}
