package org.polyfrost.polydium.client.perf;

//? if >= 1.21.5 {
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface CullingHead {
    ClassValue<Boolean> PLAIN_HEAD_SLOT = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                for (Class<?> c = type; c != null && c != LivingEntity.class; c = c.getSuperclass())
                    for (Method method : c.getDeclaredMethods())
                        if (!Modifier.isStatic(method.getModifiers()) && method.getReturnType() == ItemStack.class && method.getParameterCount() == 1 && method.getParameterTypes()[0] == EquipmentSlot.class)
                            return false;
                return true;
            } catch (LinkageError e) {
                return false;
            }
        }
    };

    ItemStack polydium$getCullingHead();

    void polydium$setCullingHead(ItemStack head);

    interface Tracker {
        void polydium$trackFor(CullingHead owner);
    }
}
//?}
