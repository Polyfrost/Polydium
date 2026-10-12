package org.polyfrost.polydium.client.perf;

public final class TextEffectRun {
    public int index = -1;
    public boolean extended;
    public float x0;
    public float x1;
    private float y0;
    private float y1;
    private float depth;
    private int color;
    private int shadowColor;
    private float shadowOffset;

    public boolean continues(float x0, float y0, float y1, float depth, int color, int shadowColor, float shadowOffset) {
        return this.index >= 0 && this.x1 == x0 && this.y0 == y0 && this.y1 == y1 && this.depth == depth
                && this.color == color && this.shadowColor == shadowColor && this.shadowOffset == shadowOffset;
    }

    public void start(float x0, float y0, float x1, float y1, float depth, int color, int shadowColor, float shadowOffset) {
        this.index = -1;
        this.extended = false;
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.depth = depth;
        this.color = color;
        this.shadowColor = shadowColor;
        this.shadowOffset = shadowOffset;
    }
}
