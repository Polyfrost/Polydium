package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.SwapRemoveIterator

class SwapRemoveIteratorTest {
    @Test
    fun `visits every element once while removing`() {
        val removed = mutableListOf<Int>()
        val iterator = SwapRemoveIterator<Int>(removed::add)
        iterator.list.addAll(1..6)

        val visited = mutableListOf<Int>()
        for (element in iterator.restart()) {
            visited += element
            if (element % 2 == 0 || element == 5) iterator.remove()
        }

        assertEquals((1..6).toSet(), visited.toSet())
        assertEquals(6, visited.size)
        assertEquals(setOf(2, 4, 5, 6), removed.toSet())
        assertEquals(setOf(1, 3), iterator.list.toSet())
    }
}
