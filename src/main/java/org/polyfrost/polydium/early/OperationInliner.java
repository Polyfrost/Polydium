package org.polyfrost.polydium.early;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class OperationInliner implements IExtension {
    private static final String OPERATION = "com/llamalad7/mixinextras/injector/wrapoperation/Operation";
    private static final String SUGAR_BRIDGE = "$mixinextras$bridge$";
    private static final String OPERATION_CALL_DESC = "([Ljava/lang/Object;)Ljava/lang/Object;";

    private final String[] mods;

    public OperationInliner(String... modIds) {
        mods = new String[modIds.length];
        for (int i = 0; i < modIds.length; i++)
            mods[i] = "$" + modIds[i] + "$";
    }

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
        for (MethodNode method : new ArrayList<>(target.methods))
            for (String mod : mods)
                if (method.name.contains(mod) && !method.name.contains(SUGAR_BRIDGE))
                    inline(target, method);
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }

    private static void inline(ClassNode owner, MethodNode handler) {
        Type[] params = Type.getArgumentTypes(handler.desc);
        boolean isStatic = (handler.access & Opcodes.ACC_STATIC) != 0;
        int opSlot = operationSlot(handler);
        if (opSlot == -1)
            return;

        MethodInsnNode call = null;
        VarInsnNode opLoad = null;
        for (AbstractInsnNode insn : handler.instructions) {
            if (insn instanceof VarInsnNode load && load.var == opSlot) {
                if (call != null || load.getOpcode() != Opcodes.ALOAD)
                    return;
                opLoad = load;
                call = operationCall(load);
                if (call == null)
                    return;
            }
        }
        if (call == null)
            return;

        Call caller = uniqueCall(owner, handler);
        while (caller != null && caller.method.name.contains(SUGAR_BRIDGE) && passesOperationOnce(caller.method))
            caller = uniqueCall(owner, caller.method);
        if (caller == null || caller.method.name.contains(SUGAR_BRIDGE))
            return;
        MethodNode siteMethod = caller.method;
        MethodInsnNode site = caller.insn;

        InvokeDynamicInsnNode indy = null;
        for (AbstractInsnNode before = site.getPrevious(); before != null && !(before instanceof JumpInsnNode); before = before.getPrevious())
            if (before instanceof InvokeDynamicInsnNode found && Type.getReturnType(found.desc).getInternalName().equals(OPERATION)) {
                indy = found;
                break;
            }
        if (indy == null || indy.bsmArgs.length < 2 || !(indy.bsmArgs[1] instanceof Handle bridgeHandle) || !bridgeHandle.getOwner().equals(owner.name))
            return;
        Type[] captured = Type.getArgumentTypes(indy.desc);
        boolean capturesThis = captured.length == 1 && captured[0].getInternalName().equals(owner.name);
        if (captured.length > 1 || captured.length == 1 && (!capturesThis || isStatic) || (siteMethod.access & Opcodes.ACC_STATIC) != 0 && capturesThis)
            return;

        MethodInsnNode original = bridgeTarget(owner, bridgeHandle);
        if (original == null)
            return;

        Type[] originalArgs = Type.getArgumentTypes(original.desc);
        boolean receiverInArray = original.getOpcode() != Opcodes.INVOKESTATIC && !capturesThis;
        if (original.getOpcode() != Opcodes.INVOKESTATIC && capturesThis && !original.owner.equals(owner.name))
            return;
        List<Type> elementTypes = new ArrayList<>();
        if (receiverInArray)
            elementTypes.add(Type.getObjectType(original.owner));
        elementTypes.addAll(List.of(originalArgs));

        InsnList fast = new InsnList();
        if (capturesThis)
            fast.add(new VarInsnNode(Opcodes.ALOAD, 0));
        AbstractInsnNode end = arrayLoads(opLoad, elementTypes, fast, params, isStatic);
        if (end != call)
            return;
        fast.add(new MethodInsnNode(original.getOpcode(), original.owner, original.name, original.desc, original.itf));

        Type result = Type.getReturnType(original.desc);
        AbstractInsnNode last = call;
        AbstractInsnNode next = call.getNext();
        if (result.getSort() == Type.VOID) {
            if (next == null || next.getOpcode() != Opcodes.POP)
                return;
            last = next;
        } else if (result.getSort() <= Type.DOUBLE) {
            String box = boxType(result);
            if (!(next instanceof TypeInsnNode cast) || cast.getOpcode() != Opcodes.CHECKCAST || !cast.desc.equals(box)
                    || !(next.getNext() instanceof MethodInsnNode unbox) || !unbox.owner.equals(box) || !unbox.name.equals(result.getClassName() + "Value"))
                return;
            last = unbox;
        } else if (next instanceof TypeInsnNode cast && cast.getOpcode() == Opcodes.CHECKCAST) {
            last = cast;
        }

        LabelNode slow = new LabelNode(), done = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, opSlot));
        guard.add(new JumpInsnNode(Opcodes.IFNONNULL, slow));
        guard.add(fast);
        guard.add(new JumpInsnNode(Opcodes.GOTO, done));
        guard.add(slow);
        handler.instructions.insertBefore(opLoad, guard);
        handler.instructions.insert(last, done);

        if (capturesThis)
            siteMethod.instructions.insertBefore(indy, new InsnNode(Opcodes.POP));
        siteMethod.instructions.set(indy, new InsnNode(Opcodes.ACONST_NULL));
    }

    private static int operationSlot(MethodNode method) {
        int found = -1, slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        for (Type param : Type.getArgumentTypes(method.desc)) {
            if (param.getInternalName().equals(OPERATION)) {
                if (found != -1)
                    return -1;
                found = slot;
            }
            slot += param.getSize();
        }
        return found;
    }

    private record Call(MethodNode method, MethodInsnNode insn) {
    }

    private static Call uniqueCall(ClassNode owner, MethodNode target) {
        Call site = null;
        for (MethodNode method : owner.methods)
            for (AbstractInsnNode insn : method.instructions)
                if (insn instanceof MethodInsnNode invoke && invoke.owner.equals(owner.name) && invoke.name.equals(target.name) && invoke.desc.equals(target.desc)) {
                    if (site != null)
                        return null;
                    site = new Call(method, invoke);
                }
        return site;
    }

    private static boolean passesOperationOnce(MethodNode bridge) {
        int slot = operationSlot(bridge), loads = 0;
        if (slot == -1)
            return false;
        for (AbstractInsnNode insn : bridge.instructions)
            if (insn instanceof VarInsnNode var && var.var == slot && (var.getOpcode() != Opcodes.ALOAD || ++loads > 1))
                return false;
        return loads == 1;
    }

    private static MethodInsnNode operationCall(VarInsnNode opLoad) {
        AbstractInsnNode insn = opLoad.getNext();
        while (insn != null && !(insn instanceof MethodInsnNode invoke && invoke.owner.equals(OPERATION)))
            insn = insn.getNext();
        return insn instanceof MethodInsnNode invoke && invoke.name.equals("call") && invoke.desc.equals(OPERATION_CALL_DESC) ? invoke : null;
    }

    private static AbstractInsnNode arrayLoads(VarInsnNode opLoad, List<Type> types, InsnList out, Type[] params, boolean isStatic) {
        AbstractInsnNode insn = opLoad.getNext();
        if (intConst(insn) != types.size())
            return null;
        insn = insn.getNext();
        if (!(insn instanceof TypeInsnNode array) || !array.desc.equals("java/lang/Object"))
            return null;
        insn = insn.getNext();
        for (int i = 0; i < types.size(); i++) {
            Type type = types.get(i);
            if (insn.getOpcode() != Opcodes.DUP || intConst(insn.getNext()) != i || !(insn.getNext().getNext() instanceof VarInsnNode load)
                    || load.getOpcode() != type.getOpcode(Opcodes.ILOAD) || !type.equals(paramAt(params, isStatic, load.var)))
                return null;
            insn = load.getNext();
            if (type.getSort() <= Type.DOUBLE) {
                if (!(insn instanceof MethodInsnNode box) || box.getOpcode() != Opcodes.INVOKESTATIC || !box.name.equals("valueOf") || !box.owner.equals(boxType(type)))
                    return null;
                insn = insn.getNext();
            }
            if (insn.getOpcode() != Opcodes.AASTORE)
                return null;
            insn = insn.getNext();
            out.add(new VarInsnNode(load.getOpcode(), load.var));
        }
        return insn;
    }

    private static Type paramAt(Type[] params, boolean isStatic, int var) {
        int slot = isStatic ? 0 : 1;
        for (Type param : params) {
            if (slot == var)
                return param;
            slot += param.getSize();
        }
        return null;
    }

    private static MethodInsnNode bridgeTarget(ClassNode owner, Handle handle) {
        for (MethodNode method : owner.methods) {
            if (!method.name.equals(handle.getName()) || !method.desc.equals(handle.getDesc()))
                continue;

            MethodInsnNode target = null;
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof InvokeDynamicInsnNode)
                    return null;
                if (insn instanceof MethodInsnNode invoke && !invoke.owner.startsWith("java/") && !invoke.owner.startsWith("com/llamalad7/")) {
                    if (target != null)
                        return null;
                    target = invoke;
                }
            }
            return target;
        }
        return null;
    }

    private static int intConst(AbstractInsnNode insn) {
        if (insn == null)
            return -1;
        int opcode = insn.getOpcode();
        if (opcode >= Opcodes.ICONST_0 && opcode <= Opcodes.ICONST_5)
            return opcode - Opcodes.ICONST_0;
        if ((opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) && insn instanceof IntInsnNode push)
            return push.operand;
        return -1;
    }

    private static String boxType(Type primitive) {
        return switch (primitive.getSort()) {
            case Type.BOOLEAN -> "java/lang/Boolean";
            case Type.CHAR -> "java/lang/Character";
            case Type.BYTE -> "java/lang/Byte";
            case Type.SHORT -> "java/lang/Short";
            case Type.INT -> "java/lang/Integer";
            case Type.FLOAT -> "java/lang/Float";
            case Type.LONG -> "java/lang/Long";
            default -> "java/lang/Double";
        };
    }
}
