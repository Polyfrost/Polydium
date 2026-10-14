package org.polyfrost.polydium.vulkan;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class IrisVulkanGate {
  private IrisVulkanGate() {
  }

  public static void apply() {
    try {
      List<IMixinConfig> configs = new ArrayList<>();
      for (Config config : Mixins.getConfigs()) configs.add(config.getConfig());
      for (Object config : (List<?>) field(processor(), "pendingConfigs")) configs.add((IMixinConfig) config);

      for (IMixinConfig config : configs) {
        if (!config.getMixinPackage().startsWith("net.irisshaders.iris.")) continue;
        for (String list : new String[]{"mixinClasses", "mixinClassesClient", "mixinClassesServer"}) {
          Field field = config.getClass().getDeclaredField(list);
          field.setAccessible(true);
          if (field.get(config) != null) field.set(config, new ArrayList<String>());
        }
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot switch Iris off for the Vulkan backend", e);
    }
  }

  static Object processor() throws ReflectiveOperationException {
    Field transformer = MixinEnvironment.class.getDeclaredField("transformer");
    transformer.setAccessible(true);
    return field(transformer.get(null), "processor");
  }

  static Object field(Object owner, String name) throws ReflectiveOperationException {
    for (int hop = 0; hop < 4; hop++) {
      Object delegate = null;
      for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
        for (Field field : type.getDeclaredFields()) {
          if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
          field.setAccessible(true);
          if (field.getName().equals(name)) return field.get(owner);
          if (field.get(owner) instanceof IMixinTransformer inner && inner != owner) delegate = inner;
        }
      }
      if (delegate == null) break;
      owner = delegate;
    }
    throw new NoSuchFieldException(name + " in " + owner.getClass().getName());
  }
}
