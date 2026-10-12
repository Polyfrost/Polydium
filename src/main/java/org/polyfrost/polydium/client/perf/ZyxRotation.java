package org.polyfrost.polydium.client.perf;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Options;

public final class ZyxRotation {
    private static final int ROTATION_PROPERTIES = Matrix4fc.PROPERTY_AFFINE | Matrix4fc.PROPERTY_ORTHONORMAL;
    private static final int ROTATED_PROPERTIES = ~(Matrix4fc.PROPERTY_PERSPECTIVE | Matrix4fc.PROPERTY_IDENTITY | Matrix4fc.PROPERTY_TRANSLATION);

    private ZyxRotation() {
    }

    public static void rotate(Matrix4f pose, Matrix3f normal, float angleZ, float angleY, float angleX) {
        float sinX = sin(angleX);
        float cosX = cos(sinX, angleX);
        float sinY = sin(angleY);
        float cosY = cos(sinY, angleY);
        float sinZ = sin(angleZ);
        float cosZ = cos(sinZ, angleZ);
        rotate(pose, sinX, cosX, sinY, cosY, sinZ, cosZ);
        rotate(normal, sinX, cosX, sinY, cosY, sinZ, cosZ);
    }

    private static float sin(float angle) {
        if (angle == 0.0F && !Options.FASTMATH) {
            return angle;
        }
        return Math.sin(angle);
    }

    private static float cos(float sin, float angle) {
        if (angle == 0.0F && !Options.FASTMATH) {
            return 1.0F;
        }
        return Math.cosFromSin(sin, angle);
    }

    private static void rotate(Matrix4f m, float sinX, float cosX, float sinY, float cosY, float sinZ, float cosZ) {
        int properties = m.properties();
        if ((properties & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            rotation(m, sinX, cosX, sinY, cosY, sinZ, cosZ);
            return;
        }
        if ((properties & Matrix4fc.PROPERTY_TRANSLATION) != 0) {
            float x = m.m30();
            float y = m.m31();
            float z = m.m32();
            rotation(m, sinX, cosX, sinY, cosY, sinZ, cosZ);
            m.setTranslation(x, y, z);
            return;
        }
        float mSinZ = -sinZ;
        float mSinY = -sinY;
        float mSinX = -sinX;
        float nm00 = m.m00() * cosZ + m.m10() * sinZ;
        float nm01 = m.m01() * cosZ + m.m11() * sinZ;
        float nm02 = m.m02() * cosZ + m.m12() * sinZ;
        float nm10 = m.m00() * mSinZ + m.m10() * cosZ;
        float nm11 = m.m01() * mSinZ + m.m11() * cosZ;
        float nm12 = m.m02() * mSinZ + m.m12() * cosZ;
        float nm20 = nm00 * sinY + m.m20() * cosY;
        float nm21 = nm01 * sinY + m.m21() * cosY;
        float nm22 = nm02 * sinY + m.m22() * cosY;
        if ((properties & Matrix4fc.PROPERTY_AFFINE) != 0) {
            m.set(
                    nm00 * cosY + m.m20() * mSinY, nm01 * cosY + m.m21() * mSinY, nm02 * cosY + m.m22() * mSinY, 0.0F,
                    nm10 * cosX + nm20 * sinX, nm11 * cosX + nm21 * sinX, nm12 * cosX + nm22 * sinX, 0.0F,
                    nm10 * mSinX + nm20 * cosX, nm11 * mSinX + nm21 * cosX, nm12 * mSinX + nm22 * cosX, 0.0F,
                    m.m30(), m.m31(), m.m32(), m.m33()
            );
        } else {
            float nm03 = m.m03() * cosZ + m.m13() * sinZ;
            float nm13 = m.m03() * mSinZ + m.m13() * cosZ;
            float nm23 = nm03 * sinY + m.m23() * cosY;
            m.set(
                    nm00 * cosY + m.m20() * mSinY, nm01 * cosY + m.m21() * mSinY, nm02 * cosY + m.m22() * mSinY, nm03 * cosY + m.m23() * mSinY,
                    nm10 * cosX + nm20 * sinX, nm11 * cosX + nm21 * sinX, nm12 * cosX + nm22 * sinX, nm13 * cosX + nm23 * sinX,
                    nm10 * mSinX + nm20 * cosX, nm11 * mSinX + nm21 * cosX, nm12 * mSinX + nm22 * cosX, nm13 * mSinX + nm23 * cosX,
                    m.m30(), m.m31(), m.m32(), m.m33()
            );
        }
        m.assume(properties & ROTATED_PROPERTIES);
    }

    private static void rotation(Matrix4f m, float sinX, float cosX, float sinY, float cosY, float sinZ, float cosZ) {
        float nm20 = cosZ * sinY;
        float nm21 = sinZ * sinY;
        m.set(
                cosZ * cosY, sinZ * cosY, -sinY, 0.0F,
                -sinZ * cosX + nm20 * sinX, cosZ * cosX + nm21 * sinX, cosY * sinX, 0.0F,
                -sinZ * -sinX + nm20 * cosX, cosZ * -sinX + nm21 * cosX, cosY * cosX, 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F
        );
        m.assume(ROTATION_PROPERTIES);
    }

    private static void rotate(Matrix3f m, float sinX, float cosX, float sinY, float cosY, float sinZ, float cosZ) {
        float mSinZ = -sinZ;
        float mSinY = -sinY;
        float mSinX = -sinX;
        float nm00 = m.m00 * cosZ + m.m10 * sinZ;
        float nm01 = m.m01 * cosZ + m.m11 * sinZ;
        float nm02 = m.m02 * cosZ + m.m12 * sinZ;
        float nm10 = m.m00 * mSinZ + m.m10 * cosZ;
        float nm11 = m.m01 * mSinZ + m.m11 * cosZ;
        float nm12 = m.m02 * mSinZ + m.m12 * cosZ;
        float nm20 = nm00 * sinY + m.m20 * cosY;
        float nm21 = nm01 * sinY + m.m21 * cosY;
        float nm22 = nm02 * sinY + m.m22 * cosY;
        m.m00 = nm00 * cosY + m.m20 * mSinY;
        m.m01 = nm01 * cosY + m.m21 * mSinY;
        m.m02 = nm02 * cosY + m.m22 * mSinY;
        m.m10 = nm10 * cosX + nm20 * sinX;
        m.m11 = nm11 * cosX + nm21 * sinX;
        m.m12 = nm12 * cosX + nm22 * sinX;
        m.m20 = nm10 * mSinX + nm20 * cosX;
        m.m21 = nm11 * mSinX + nm21 * cosX;
        m.m22 = nm12 * mSinX + nm22 * cosX;
    }
}
