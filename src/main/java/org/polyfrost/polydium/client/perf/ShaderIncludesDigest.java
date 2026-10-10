package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public interface ShaderIncludesDigest {
    byte[] polydium$includesDigest();

    void polydium$setIncludesDigest(byte[] digest);
}
//?}
