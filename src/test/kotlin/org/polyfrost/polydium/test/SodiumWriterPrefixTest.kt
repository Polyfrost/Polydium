package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin

class SodiumWriterPrefixTest {
    private val writer = "net/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter"

    @Test
    fun `only writer calls are prefixed, and only once across repeated mixin applications`() {
        val calls = listOf(
            MethodInsnNode(Opcodes.INVOKESTATIC, writer, "tryOf", "()V", true),
            MethodInsnNode(Opcodes.INVOKEINTERFACE, writer, "sodium\$push", "()V", true),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/lwjgl/system/MemoryStack", "push", "()V", false),
        )
        val target = ClassNode().apply {
            methods.add(MethodNode(Opcodes.ACC_STATIC, "render", "()V", null, null).apply { calls.forEach(instructions::add) })
        }

        repeat(2) { PolydiumMixinPlugin.prefixSodiumWriterCalls(target) }

        assertEquals(listOf("sodium\$tryOf", "sodium\$push", "push"), calls.map { it.name })
    }
}
