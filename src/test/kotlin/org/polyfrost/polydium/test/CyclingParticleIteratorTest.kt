package org.polyfrost.polydium.test

import com.google.common.collect.EvictingQueue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.CyclingParticleIterator
import java.util.ArrayDeque
import java.util.Random

class CyclingParticleIteratorTest {
    @Test
    fun `matches iterator removal`() {
        val random = Random(1)
        repeat(2_000) {
            val size = random.nextInt(40)
            val dead = (0 until size).filter { random.nextInt(3) == 0 }.toSet()
            val expected = ArrayDeque((0 until size).toList())
            val expectedVisits = mutableListOf<Int>()
            val vanilla = expected.iterator()
            while (vanilla.hasNext()) {
                val p = vanilla.next(); expectedVisits += p
                if (p in dead) vanilla.remove()
            }
            for (queue in listOf(ArrayDeque<Int>(), EvictingQueue.create<Int>(maxOf(size, 1)))) {
                queue.addAll(0 until size)
                val visits = mutableListOf<Int>()
                val cycling = CyclingParticleIterator.of(queue)!!
                while (cycling.hasNext()) {
                    val p = cycling.next(); visits += p
                    if (p in dead) cycling.remove()
                }
                assertEquals(expectedVisits, visits)
                assertEquals(expected.toList(), queue.toList())
            }
        }
    }
}
