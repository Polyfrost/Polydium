package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TransformWrites {
    //? if >= 26.3 {
    public static final int SIZE = 156;
    //?} else if >= 1.21.11 {
    /*public static final int SIZE = 160;
    *///?} else {
    /*public static final int SIZE = 164;
    *///?}

    private TransformWrites() {
    }

    public static boolean write(ByteBuffer buffer, Matrix4fc modelView, Vector4fc colorModulator, Vector3fc modelOffset, Matrix4fc textureMatrix/*? if < 1.21.11 {*//*, float lineWidth*//*?}*/) {
        int position = buffer.position();
        if (!buffer.isDirect() || buffer.isReadOnly() || buffer.order() != ByteOrder.nativeOrder() || buffer.limit() - position < SIZE) {
            return false;
        }
        long address = MemoryUtil.memAddress(buffer);
        modelView.getToAddress(address);
        //? if >= 26.3 {
        textureMatrix.getToAddress(address + 64L);
        colorModulator.getToAddress(address + 128L);
        modelOffset.getToAddress(address + 144L);
        //?} else {
        /*colorModulator.getToAddress(address + 64L);
        modelOffset.getToAddress(address + 80L);
        textureMatrix.getToAddress(address + 96L);
        *///?}
        //? if < 1.21.11
        /*MemoryUtil.memPutFloat(address + 160L, lineWidth);*/
        buffer.position(position + SIZE);
        return true;
    }
}
//?}
