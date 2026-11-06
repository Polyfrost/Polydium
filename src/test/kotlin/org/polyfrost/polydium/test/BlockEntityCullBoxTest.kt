package org.polyfrost.polydium.test

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.BlockEntityCullBox

class BlockEntityCullBoxTest {
    private class DefaultBounds

    private class OwnBounds {
        @Suppress("unused")
        fun getRenderBoundingBox(blockEntity: BlockEntity): AABB = throw UnsupportedOperationException()
    }

    @Test
    fun `only the default box is fixed`() {
        assertTrue(BlockEntityCullBox.fixedBounds(DefaultBounds::class.java))
        assertFalse(BlockEntityCullBox.fixedBounds(OwnBounds::class.java))
    }
}
