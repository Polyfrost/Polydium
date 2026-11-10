package org.polyfrost.polydium.early;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public final class LocalRefHoister implements IExtension {
    private static final String TARGET = "net/caffeinemc/mods/sodium/client/render/chunk/compile/tasks/ChunkBuilderMeshingTask";

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {
    }

    @Override
    public void postApply(ITargetClassContext context) {
        ClassNode target = context.getClassNode();
        if (!target.name.equals(TARGET))
            return;
        for (MethodNode method : target.methods)
            if (method.name.equals("execute"))
                hoist(method);
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }

    private static void hoist(MethodNode method) {
        InsnList head = new InsnList();
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof TypeInsnNode alloc) || alloc.getOpcode() != Opcodes.NEW || !alloc.desc.contains("/sugar/impl/ref/generated/"))
                continue;

            AbstractInsnNode dup = alloc.getNext(), init = dup.getNext(), store = init.getNext();
            if (dup.getOpcode() != Opcodes.DUP || !(init instanceof MethodInsnNode ctor) || !ctor.name.equals("<init>") || !ctor.desc.equals("()V")
                    || !(store instanceof VarInsnNode slot) || slot.getOpcode() != Opcodes.ASTORE || !inLoop(method, alloc))
                continue;

            int hoisted = method.maxLocals++;
            head.add(new TypeInsnNode(Opcodes.NEW, alloc.desc));
            head.add(new InsnNode(Opcodes.DUP));
            head.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, alloc.desc, "<init>", "()V", false));
            head.add(new VarInsnNode(Opcodes.ASTORE, hoisted));

            method.instructions.insertBefore(alloc, new VarInsnNode(Opcodes.ALOAD, hoisted));
            method.instructions.remove(alloc);
            method.instructions.remove(dup);
            method.instructions.remove(init);
        }
        method.instructions.insert(head);
    }

    private static boolean inLoop(MethodNode method, AbstractInsnNode insn) {
        int at = method.instructions.indexOf(insn);
        for (AbstractInsnNode other : method.instructions)
            if (other instanceof JumpInsnNode jump && method.instructions.indexOf(jump.label) <= at && at < method.instructions.indexOf(jump))
                return true;
        return false;
    }
}
