package org.polyfrost.polydium.test

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.NameTagReuse

class NameTagReuseTest {
    private fun name(text: String) = Component.literal(text).withStyle(ChatFormatting.RED)
        .append(Component.translatable("chat.type.text", Component.literal(text), "x"))

    @Test
    fun `static trees are remembered and match equal fresh copies`() {
        val remembered = NameTagReuse.remembered(name("Steve"))
        assertNotNull(remembered)
        assertTrue(NameTagReuse.matches(remembered, name("Steve")))
        assertFalse(NameTagReuse.matches(remembered, name("Alex")))
        assertFalse(NameTagReuse.matches(remembered, name("Steve").withStyle(ChatFormatting.BOLD)))
        assertFalse(NameTagReuse.matches(null, name("Steve")))
    }

    @Test
    fun `key binding names are never remembered`() {
        assertNull(NameTagReuse.remembered(Component.keybind("key.jump")))
        assertNull(NameTagReuse.remembered(Component.literal("a").append(Component.keybind("key.jump"))))
        assertNull(NameTagReuse.remembered(Component.translatable("chat.type.text", Component.keybind("key.jump"), "x")))
        assertNull(NameTagReuse.remembered(null))
    }
}
