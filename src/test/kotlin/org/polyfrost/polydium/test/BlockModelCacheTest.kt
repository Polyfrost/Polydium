package org.polyfrost.polydium.test

//? if >= 26.1 {
import net.minecraft.SharedConstants
import net.minecraft.client.renderer.block.BlockModelRenderState
import net.minecraft.client.renderer.block.BlockModelSet
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.server.Bootstrap
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.FrameModels

class BlockModelCacheTest {
    @Test
    fun `a replacement model set starts with its own render caches`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val old = modelSet()
        val current = modelSet()
        val key = Blocks.STONE.defaultBlockState()
        val state = BlockModelRenderState()

        old.`polydium$displayModels`()[key] = state

        assertNotSame(old.`polydium$frameModels`(), current.`polydium$frameModels`())
        assertSame(state, old.`polydium$displayModels`()[key])
        assertFalse(current.`polydium$displayModels`().containsKey(key))
    }

    private fun modelSet(): FrameModels.Owner {
        // This fixture only exercises cache ownership, so no fallback models or block colors are needed.
        return BlockModelSet::class.java.constructors.single()
            .newInstance(null, emptyMap<BlockState, BlockModel>(), null) as FrameModels.Owner
    }
}
//?}
