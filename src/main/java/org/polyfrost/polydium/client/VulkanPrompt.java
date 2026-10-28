package org.polyfrost.polydium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;
import org.polyfrost.polydium.compat.Mods;
//? if >= 1.21.11 {
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.resources.Identifier;
import org.polyfrost.polydium.early.EarlyVulkan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
//?}
//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.PreferredGraphicsApi;
//?} else if >= 1.21.11 {
/*import org.polyfrost.polydium.vulkan.GraphicsApi;
import org.polyfrost.polydium.vulkan.VulkanActive;
*///?}

public final class VulkanPrompt {
    //? if >= 1.21.11
    private static final Identifier IMAGE = Identifier.fromNamespaceAndPath("polydium", "textures/gui/vulkan_prompt.png");

    private static boolean checked;
    private static boolean vulkanModNoticed;

    private VulkanPrompt() {
    }

    public static final class Notice extends AlertScreen {
        public static final int MAX_WIDTH = 320;

        private Notice(Runnable callback) {
            super(callback, Component.translatable("polydium.vulkanModNotice.title"), Component.translatable("polydium.vulkanModNotice"));
        }
    }

    public static void tick(Minecraft mc) {
        if (Mods.VULKANMOD) {
            if (!vulkanModNoticed && mc.player != null && mc/*? if >= 26.2 {*/.gui.screen()/*?} else {*//*.screen*//*?}*/ == null) {
                vulkanModNoticed = true;
                mc/*? if >= 26.2 {*/.gui/*?}*/.setScreen(new Notice(() -> mc/*? if >= 26.2 {*/.gui/*?}*/.setScreen(null)));
            }
            return;
        }
        //? if >= 1.21.11 {
        if (checked || mc.player == null || mc/*? if >= 26.2 {*/.gui.screen()/*?} else {*//*.screen*//*?}*/ != null)
            return;
        checked = true;
        List<Path> markers = markers();
        if (markers.stream().anyMatch(Files::exists) || EarlyVulkan.switched())
            return;
        //? if >= 26.2 {
        if ("Vulkan".equalsIgnoreCase(RenderSystem.getDevice().getDeviceInfo().backendName()))
            return;
        String key = "polydium.vulkanPrompt.message";
        //?} else {
        /*if (VulkanActive.active)
            return;
        String key = "polydium.vulkanPrompt.message.backport";
        *///?}
        if (VulkanAvailability.unavailableReason() != null)
            return;
        for (Path marker : markers) {
            try {
                Files.createDirectories(marker.getParent());
                Files.writeString(marker, "");
                break;
            } catch (IOException ignored) {
            }
        }
        mc/*? if >= 26.2 {*/.gui/*?}*/.setScreen(new ConfirmScreen(yes -> {
            if (yes) {
                //? if >= 26.2 {
                mc.options.preferredGraphicsBackend().set(PreferredGraphicsApi.VULKAN);
                mc.options.save();
                //?} else {
                /*GraphicsApi.OPTION.set(GraphicsApi.VULKAN);
                *///?}
            }
            mc/*? if >= 26.2 {*/.gui/*?}*/.setScreen(null);
        }, Component.translatable("polydium.vulkanPrompt.title"), Component.translatable(key),
                Component.translatable("polydium.vulkanPrompt.yes"), Component.translatable("polydium.vulkanPrompt.no")) {
            @Override
            protected void addAdditionalText() {
                int height = Math.min(171, this.height / 3);
                int width = height * 502 / 342;
                this.layout.addChild(ImageWidget.texture(width, height, IMAGE, width, height));
                this.layout.addChild(new StringWidget(Component.translatable("polydium.vulkanPrompt.restart").withStyle(ChatFormatting.YELLOW), this.font));
            }
        });
        //?}
    }

    //? if >= 1.21.11 {
    private static List<Path> markers() {
        Path local = FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("vulkan-prompt");
        try {
            String home = System.getProperty("user.home");
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            Path dir;
            if (os.startsWith("windows")) {
                String appData = System.getenv("APPDATA");
                dir = Path.of(appData != null ? appData : home, "Polydium");
            } else if (os.startsWith("mac")) {
                dir = Path.of(home, "Library", "Application Support", "Polydium");
            } else {
                String xdg = System.getenv("XDG_CONFIG_HOME");
                dir = (xdg != null && !xdg.isBlank() ? Path.of(xdg) : Path.of(home, ".config")).resolve("polydium");
            }
            return List.of(dir.resolve("vulkan-prompt"), local);
        } catch (RuntimeException e) {
            return List.of(local);
        }
    }
    //?}
}
