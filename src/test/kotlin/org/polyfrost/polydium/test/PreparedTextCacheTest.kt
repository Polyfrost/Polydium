package org.polyfrost.polydium.test

//? if >= 1.21.10 {
import com.mojang.serialization.MapCodec
import net.minecraft.network.chat.FontDescription
//?}
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
/*import net.minecraft.resources.ResourceLocation as Identifier
*///?}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.PerfClock
import org.polyfrost.polydium.client.perf.PreparedTextCache

class PreparedTextCacheTest {
    private fun lookup(cache: PreparedTextCache<String>, id: Int): String? {
        cache.begin(1)
        cache.add(0, Style.EMPTY, id)
        val cached = cache.get(0f, 0f, 0, 0, 0)
        if (cached == null) cache.put(cache.miss(), "v$id")
        return cached
    }

    private fun seen(cache: PreparedTextCache<String>, id: Int): String? {
        cache.begin(1)
        return cache.seen("t$id", 0f, 0f, 0, 0, 0)
    }

    private fun frame(cache: PreparedTextCache<String>, ids: IntRange) {
        PerfClock.frame++
        ids.forEach { lookup(cache, it) }
    }

    @Test
    fun `a frame larger than the cache keeps what is cached`() {
        val cache = PreparedTextCache<String>(4)
        repeat(3) { frame(cache, 0 until 16) }
        PerfClock.frame++
        (0 until 4).forEach { assertEquals("v$it", lookup(cache, it)) }
    }

    @Test
    fun `seen text survives a frame larger than the cache`() {
        val cache = PreparedTextCache<String>(4)
        PerfClock.frame++
        (0 until 16).forEach { seen(cache, it) }
        PerfClock.frame++
        (0 until 16).forEach { id -> if (seen(cache, id) == null) cache.remember("v$id") }
        PerfClock.frame++
        (0 until 4).forEach { assertEquals("v$it", seen(cache, it)) }
    }

    @Test
    fun `repeated hits leave room for other text`() {
        val cache = PreparedTextCache<String>(4)
        PerfClock.frame++
        repeat(8) { lookup(cache, 0) }
        (1 until 4).forEach { lookup(cache, it) }
        PerfClock.frame++
        (0 until 4).forEach { assertEquals("v$it", lookup(cache, it)) }
    }

    @Test
    fun `repeated seen hits leave room for other text`() {
        val cache = PreparedTextCache<String>(4)
        PerfClock.frame++
        repeat(8) { seen(cache, 0) }
        (1 until 4).forEach { seen(cache, it) }
        PerfClock.frame++
        (1 until 4).forEach { id -> assertNull(seen(cache, id)); cache.remember("v$id") }
        PerfClock.frame++
        (1 until 4).forEach { assertEquals("v$it", seen(cache, it)) }
    }

    @Test
    fun `text from older frames still ages out`() {
        val cache = PreparedTextCache<String>(4)
        frame(cache, 0 until 4)
        frame(cache, 4 until 8)
        PerfClock.frame++
        assertNull(lookup(cache, 0))
        assertEquals("v7", lookup(cache, 7))
    }

    //? if >= 1.21.8 {
    private class UnhashableContents : ComponentContents {
        override fun hashCode(): Int = throw AssertionError("hover component was hashed")

        override fun equals(other: Any?): Boolean = this === other

        //? if >= 1.21.10 {
        override fun codec(): MapCodec<out ComponentContents> = throw UnsupportedOperationException()
        //?} else {
        /*override fun type(): ComponentContents.Type<*> = throw UnsupportedOperationException()
        *///?}
    }

    //? if >= 1.21.10 {
    private val uniform = FontDescription.Resource(Identifier.withDefaultNamespace("uniform"))
    //?} else {
    /*private val uniform = Identifier.withDefaultNamespace("uniform")
    *///?}

    private fun hover(text: Component): Style = Style.EMPTY.withHoverEvent(HoverEvent.ShowText(text))

    private fun lookup(cache: PreparedTextCache<String>, style: Style, value: String): String? {
        cache.begin(1)
        "name".forEachIndexed { index, char -> cache.add(index, style, char.code) }
        val cached = cache.get(0f, 0f, 0, 0, 0)
        if (cached == null) cache.put(cache.miss(), value)
        return cached
    }

    @Test
    fun `hover does not change the render hash but still separates entries`() {
        val steve = hover(Component.literal("Steve"))
        val alex = hover(Component.literal("Alex"))
        assertNotEquals(steve, alex)
        assertTrue(PreparedTextCache.renderEquals(steve, alex))
        assertEquals(PreparedTextCache.renderHash(steve), PreparedTextCache.renderHash(alex))

        val cache = PreparedTextCache<String>(16)
        PerfClock.frame++
        assertNull(lookup(cache, steve, "steve"))
        assertNull(lookup(cache, alex, "alex"))
        assertEquals("steve", lookup(cache, hover(Component.literal("Steve")), "copy"))
        assertEquals("alex", lookup(cache, alex, "copy"))
    }

    @Test
    fun `color, bold and font changes miss`() {
        val styles = listOf(Style.EMPTY, Style.EMPTY.withColor(0xFF0000), Style.EMPTY.withBold(true), Style.EMPTY.withFont(uniform))
        for (i in styles.indices) {
            for (j in styles.indices) {
                if (i != j) assertFalse(PreparedTextCache.renderEquals(styles[i], styles[j]))
            }
        }

        val cache = PreparedTextCache<String>(16)
        PerfClock.frame++
        styles.forEachIndexed { i, style -> assertNull(lookup(cache, style, "v$i")) }
        styles.forEachIndexed { i, style -> assertEquals("v$i", lookup(cache, style, "copy")) }
    }

    @Test
    fun `deep hover components are never hashed`() {
        val style = hover(MutableComponent.create(UnhashableContents()))
        assertThrows(AssertionError::class.java) { style.hashCode() }

        PreparedTextCache.renderHash(style)
        val cache = PreparedTextCache<String>(16)
        PerfClock.frame++
        assertNull(lookup(cache, style, "deep"))
        assertNull(lookup(cache, hover(Component.literal("other")), "other"))
        assertEquals("deep", lookup(cache, style, "copy"))
        assertEquals("other", lookup(cache, hover(Component.literal("other")), "copy"))
    }
    //?}
}
