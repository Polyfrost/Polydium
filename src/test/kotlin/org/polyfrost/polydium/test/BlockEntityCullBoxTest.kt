package org.polyfrost.polydium.test

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
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

    @Test
    fun `a box does not outlive its renderer`() {
        val holder = object : BlockEntityCullBox {
            var box: AABB? = null
            var renderer: Any? = null
            override fun `polydium$storedCullBox`() = box
            override fun `polydium$cullBoxRenderer`() = renderer
            override fun `polydium$setCullBox`(box: AABB?, renderer: Any?) {
                this.box = box
                this.renderer = renderer
            }
        }
        val box = AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
        val renderer = Any()
        assertNull(holder.`polydium$cullBox`(renderer))
        holder.`polydium$setCullBox`(box, renderer)
        assertSame(box, holder.`polydium$cullBox`(renderer))
        assertNull(holder.`polydium$cullBox`(Any()))
    }
}
