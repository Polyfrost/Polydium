package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.QuadSort
import java.util.Random

class QuadSortTest {
    private fun expected(keys: IntArray): IntArray =
        keys.indices.sortedWith(compareBy<Int>({ keys[it] }, { it })).toIntArray()

    @Test
    fun `sorts by signed key and keeps equal keys in index order`() {
        val random = Random(42L)
        for (length in intArrayOf(2, 50, 3, 300, 7, 5000, 64, 1)) {
            val keys = IntArray(length) { random.nextInt(9) - 4 + if (it % 5 == 0) Int.MIN_VALUE / 2 else 0 }
            val perm = IntArray(length) { it }
            QuadSort.sortSmall(perm, keys)
            assertArrayEquals(expected(keys), perm, "length $length")
        }
    }

    @Test
    fun `ignores stale scratch from a longer earlier sort`() {
        val long = IntArray(200) { 200 - it }
        QuadSort.sortSmall(IntArray(200) { it }, long)

        val keys = intArrayOf(5, -1, 5, 0)
        val perm = intArrayOf(0, 1, 2, 3)
        QuadSort.sortSmall(perm, keys)
        assertArrayEquals(intArrayOf(1, 3, 0, 2), perm)
    }

    @Test
    fun `sorts a permuted input`() {
        val keys = intArrayOf(3, 1, 2, 1)
        val perm = intArrayOf(3, 2, 1, 0)
        QuadSort.sortSmall(perm, keys)
        assertArrayEquals(intArrayOf(1, 3, 2, 0), perm)
    }
}
