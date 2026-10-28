package org.polyfrost.polydium.vulkan;

import com.mojang.blaze3d.platform.InputConstants;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;*/
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public final class IrisVulkan {
    private static KeyMapping shaderpackScreenKeybind;

    private IrisVulkan() {
    }

    public static void init() {
        if (!IrisVulkanGate.applied)
            return;
        shaderpackScreenKeybind = /*? if >= 26.1 {*/KeyMappingHelper.registerKeyMapping/*?} else {*//*KeyBindingHelper.registerKeyBinding*//*?}*/(new KeyMapping("iris.keybind.shaderPackSelection", InputConstants.Type.KEYSYM, 73,
                irisKeybindCategory()));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (shaderpackScreenKeybind.consumeClick())
                mc.setScreen(placeholder(null));
        });
    }

    private static KeyMapping.Category irisKeybindCategory() {
        try {
            Field field = Class.forName("net.irisshaders.iris.Iris").getDeclaredField("irisKeybindCategory");
            field.setAccessible(true);
            return (KeyMapping.Category) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot find Iris keybind category", e);
        }
    }

    public static Screen placeholder(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        return new ConfirmScreen(yes -> {
            if (!yes) {
                mc.setScreen(parent);
                return;
            }
            GraphicsApi.OPTION.set(GraphicsApi.OPENGL);
            if (mc.isLocalServer() && mc.getSingleplayerServer() != null)
                mc.getSingleplayerServer().halt(true);
            mc.disconnectWithSavingScreen();
            mc.stop();
        }, Component.literal("Iris"), Component.literal("Iris cannot run when using Vulkan. Would you like to switch to OpenGL?\nThis will close your game."),
                Component.literal("Switch"), Component.literal("Return"));
    }

    public static Object modMenuFactory(ClassLoader loader) throws ClassNotFoundException {
        Class<?> factory = Class.forName("com.terraformersmc.modmenu.api.ConfigScreenFactory", false, loader);
        return Proxy.newProxyInstance(loader, new Class<?>[]{factory}, (proxy, method, args) -> switch (method.getName()) {
            case "create" -> placeholder((Screen) args[0]);
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "IrisVulkan placeholder factory";
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    public static void selectGraphicsApi(GraphicsApi value) {
        Minecraft mc = Minecraft.getInstance();
        if (IrisVulkanGate.applied || VulkanActive.active || value != GraphicsApi.VULKAN || !FabricLoader.getInstance().isModLoaded("iris")) {
            GraphicsApi.OPTION.set(value);
            return;
        }
        Screen s = mc.screen;
        mc.setScreen(new ConfirmScreen(yes -> {
            if (yes) {
                GraphicsApi.OPTION.set(value);
                mc.setScreen(s);
            } else {
                s.onClose();
                mc.setScreen(VideoSettingsScreen.createScreen(mc.screen));
            }
        }, Component.literal("Iris"), Component.literal("Setting the backend to Vulkan will remove your ability to use shaders.\nWould you like to proceed?")));
    }
}
