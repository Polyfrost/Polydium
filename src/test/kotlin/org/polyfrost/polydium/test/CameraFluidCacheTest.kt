package org.polyfrost.polydium.test

import net.minecraft.SharedConstants
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.server.Bootstrap
import net.minecraft.world.level.material.FogType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame

class CameraFluidCacheTest {
    @Test
    fun `an uninitialized camera reports no fog without a client`() {
        assertEquals(FogType.NONE, Camera().getFluidInCamera())
    }

    @Test
    fun `reset releases the level held by the camera fluid cache`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val camera = Camera()
        val levelField = Camera::class.java.getDeclaredField("level").apply { isAccessible = true }
        val cachedLevel = Camera::class.java.getDeclaredField("polydium\$fluidLevel").apply { isAccessible = true }
        // This test only needs a level identity, so it does not start a connection or a renderer.
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafe = unsafeClass.getDeclaredField("theUnsafe").apply { isAccessible = true }.get(null)
        val level = unsafeClass.getMethod("allocateInstance", Class::class.java).invoke(unsafe, ClientLevel::class.java)
        levelField.set(camera, level)
        cachedLevel.set(camera, level)
        assertSame(level, cachedLevel.get(camera))

        camera.reset()

        assertNull(levelField.get(camera))
        assertNull(cachedLevel.get(camera))
    }
}
