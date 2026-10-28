package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy
import java.util.Random

class GlyphAtlasSkipTest {
    private class Vanilla(val x: Int, val y: Int, val width: Int, val height: Int) {
        private var left: Vanilla? = null
        private var right: Vanilla? = null
        private var occupied = false

        fun insert(w: Int, h: Int): Vanilla? {
            val l = left
            val r = right
            if (l != null && r != null) return l.insert(w, h) ?: r.insert(w, h)
            if (occupied || w > width || h > height) return null
            if (w == width && h == height) {
                occupied = true
                return this
            }
            if (width - w > height - h) {
                left = Vanilla(x, y, w, height)
                right = Vanilla(x + w + 1, y, width - w - 1, height)
            } else {
                left = Vanilla(x, y, width, h)
                right = Vanilla(x, y + h + 1, width, height - h - 1)
            }
            return left!!.insert(w, h)
        }
    }

    private class Atlas(size: Int) {
        private val tree: Any
        private val insert: (Int, Int) -> Any?
        private val xField: java.lang.reflect.Field
        private val yField: java.lang.reflect.Field

        init {
            val modern = runCatching { Class.forName("net.minecraft.client.renderer.texture.DynamicAtlasTree") }.getOrNull()
            val type = modern ?: Class.forName("net.minecraft.client.gui.font.FontTexture\$Node")
            check(type.declaredFields.any { it.name.endsWith("freeBits") }) { "free space mixin not applied" }
            val constructor = type.getDeclaredConstructor(Int::class.java, Int::class.java, Int::class.java, Int::class.java)
            constructor.isAccessible = true
            tree = constructor.newInstance(0, 0, size, size)
            xField = type.getDeclaredField("x").apply { isAccessible = true }
            yField = type.getDeclaredField("y").apply { isAccessible = true }
            if (modern != null) {
                val method = type.getMethod("insert", Int::class.java, Int::class.java, Int::class.java)
                insert = { w, h -> method.invoke(tree, w, h, 1) }
            } else {
                val method = type.declaredMethods.single { it.name == "insert" }.apply { isAccessible = true }
                val glyphType = method.parameterTypes[0]
                insert = { w, h ->
                    val glyph = Proxy.newProxyInstance(glyphType.classLoader, arrayOf(glyphType)) { _, m, _ ->
                        when (m.name) {
                            "getPixelWidth" -> w
                            "getPixelHeight" -> h
                            else -> throw UnsupportedOperationException(m.name)
                        }
                    }
                    method.invoke(tree, glyph)
                }
            }
        }

        fun insert(w: Int, h: Int): Pair<Int, Int>? {
            val node = insert.invoke(w, h) ?: return null
            return xField.getInt(node) to yField.getInt(node)
        }
    }

    @Test
    fun `places glyphs like vanilla`() {
        val random = Random(1)
        for (size in listOf(256, 1024, 256, 1024)) {
            val vanilla = Vanilla(0, 0, size, size)
            val skipping = Atlas(size)
            repeat(6_000) {
                val large = random.nextInt(40) == 0
                val w = 1 + random.nextInt(if (large) size else 24)
                val h = 1 + random.nextInt(if (large) size else 24)
                assertEquals(vanilla.insert(w, h)?.let { it.x to it.y }, skipping.insert(w, h))
            }
        }
    }
}
