package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

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
        modelView.get(position, buffer);
        //? if >= 26.3 {
        textureMatrix.get(position + 64, buffer);
        colorModulator.get(position + 128, buffer);
        modelOffset.get(position + 144, buffer);
        //?} else {
        /*colorModulator.get(position + 64, buffer);
        modelOffset.get(position + 80, buffer);
        textureMatrix.get(position + 96, buffer);
        *///?}
        //? if < 1.21.11
        /*buffer.putFloat(position + 160, lineWidth);*/
        buffer.position(position + SIZE);
        return true;
    }
}
//?}
