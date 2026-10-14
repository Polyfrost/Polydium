package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
public interface TexelViewCache {
    long polydium$findTexelView(long offset, long range, int format);

    boolean polydium$storeTexelView(long offset, long range, int format, long view);
}
//?}
