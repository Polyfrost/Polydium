package org.polyfrost.polydium.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicVerifier
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin

// Models the bytecode Mixin 0.8.7's ModifyArgsInjector leaves in processQueue for Sodium Extra 0.9.4's
// sodiumExtra$allowExpandedCylindricalVerticalTraversal(Args) on both the Sodium 0.8 and 0.9 OcclusionCuller.
class ExpandedCylindricalGateTest {
    private val owner = "net/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller"
    private val argsClass = "org/spongepowered/asm/synthetic/args/Args\$1"
    private val handlerName = "handler\$zzz000\$sodium-extra\$sodiumExtra\$allowExpandedCylindricalVerticalTraversal"
    private val handlerDesc = "(Lorg/spongepowered/asm/mixin/injection/invoke/arg/Args;)V"

    @Test
    fun `static sodium 08 culler skips args packing unless the search distance is expanded`() {
        val neighbors = "(Ljava/lang/Object;Ljava/lang/Object;II)V"
        val method = processQueue(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC, "(Ljava/lang/Object;Ljava/lang/Object;FLjava/lang/Object;)V", neighbors, static = true) {
            add(VarInsnNode(Opcodes.ALOAD, 0))
            add(VarInsnNode(Opcodes.ALOAD, 3))
            add(InsnNode(Opcodes.ICONST_1))
            add(InsnNode(Opcodes.ICONST_2))
        }
        val target = culler(method)

        PolydiumMixinPlugin.gateExpandedCylindricalTraversal(target, method)

        assertGated(target, method, listOf("isExpandedCylindricalCullDistance", "of", handlerName))
    }

    @Test
    fun `instance sodium 09 culler skips args packing unless either search distance is expanded`() {
        val neighbors = "(Ljava/lang/Object;Ljava/lang/Object;IIILjava/lang/Object;)V"
        val method = processQueue(Opcodes.ACC_PRIVATE, "(Ljava/lang/Object;Ljava/lang/Object;)V", neighbors, static = false) {
            add(VarInsnNode(Opcodes.ALOAD, 0))
            add(VarInsnNode(Opcodes.ALOAD, 1))
            add(VarInsnNode(Opcodes.ALOAD, 2))
            add(InsnNode(Opcodes.ICONST_1))
            add(InsnNode(Opcodes.ICONST_2))
            add(InsnNode(Opcodes.ICONST_3))
            add(VarInsnNode(Opcodes.ALOAD, 1))
        }
        val target = culler(method).apply {
            fields.add(FieldNode(Opcodes.ACC_PRIVATE, "searchDistanceRegular", "F", null, null))
            fields.add(FieldNode(Opcodes.ACC_PRIVATE, "searchDistanceLocal", "F", null, null))
        }

        PolydiumMixinPlugin.gateExpandedCylindricalTraversal(target, method)

        assertGated(target, method, listOf("isExpandedCylindricalCullDistance", "isExpandedCylindricalCullDistance", "of", handlerName))
    }

    private fun culler(method: MethodNode) = ClassNode().apply {
        version = Opcodes.V21
        name = owner
        superName = "java/lang/Object"
        methods.add(method)
    }

    private fun processQueue(access: Int, desc: String, neighbors: String, static: Boolean, pushArgs: InsnList.() -> Unit) =
        MethodNode(access, "processQueue", desc, null, null).apply {
            val args = Type.getArgumentTypes(neighbors)
            instructions.add(InsnList().apply(pushArgs))
            instructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, argsClass, "of", Type.getMethodDescriptor(Type.getObjectType(argsClass), *args), false))
            instructions.add(InsnNode(Opcodes.DUP))
            if (static) {
                instructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, owner, handlerName, handlerDesc, false))
            } else {
                instructions.add(VarInsnNode(Opcodes.ALOAD, 0))
                instructions.add(InsnNode(Opcodes.SWAP))
                instructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, owner, handlerName, handlerDesc, false))
            }
            args.forEachIndexed { i, type ->
                val last = i == args.size - 1
                if (!last) instructions.add(InsnNode(Opcodes.DUP))
                instructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, argsClass, "\$$i", "()" + type.descriptor, false))
                if (!last) instructions.add(InsnNode(Opcodes.SWAP))
            }
            instructions.add(MethodInsnNode(if (static) Opcodes.INVOKESTATIC else Opcodes.INVOKEVIRTUAL, owner, "visitNeighbors", neighbors, false))
            instructions.add(InsnNode(Opcodes.RETURN))
            maxStack = 16
            maxLocals = 8
        }

    private fun assertGated(target: ClassNode, method: MethodNode, callsBeforeHandler: List<String>) {
        val calls = method.instructions.filterIsInstance<MethodInsnNode>().map { it.name }
        assertEquals(callsBeforeHandler, calls.take(callsBeforeHandler.size))

        val neighbors = method.instructions.filterIsInstance<MethodInsnNode>().single { it.name == "visitNeighbors" }
        val skip = method.instructions.filterIsInstance<JumpInsnNode>().last()
        assertEquals(previousLabel(neighbors), skip.label)
        assert(skip.opcode == Opcodes.IFEQ || skip.opcode == Opcodes.GOTO)

        Analyzer(BasicVerifier()).analyze(target.name, method)
    }

    private fun previousLabel(insn: AbstractInsnNode): LabelNode? = insn.previous as? LabelNode
}
