package org.polyfrost.polydium.vulkan;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.polyfrost.polydium.early.EarlyVulkan;

import java.util.ArrayList;
import java.util.List;

public enum GraphicsApi implements StringRepresentable {
    DEFAULT("default"),
    OPENGL("opengl"),
    VULKAN("vulkan");

    private static final GraphicsApi STARTUP = byName(EarlyVulkan.graphicsApi());

    public static final OptionInstance<GraphicsApi> OPTION = new OptionInstance<>(
            "options.graphicsApi",
            value -> {
                List<Component> lines = new ArrayList<>();
                if (value != STARTUP) {
                    lines.add(Component.translatable("options.needsRestart"));
                    lines.add(CommonComponents.EMPTY);
                }
                lines.add(Component.translatable("options.graphicsApi.tooltip"));
                if (value == VULKAN) {
                    lines.add(CommonComponents.EMPTY);
                    lines.add(Component.translatable("options.graphicsApi.tooltip.vulkan"));
                }
                return Tooltip.create(CommonComponents.joinLines(lines));
            },
            (caption, value) -> Component.translatable("options.graphicsApi." + value.name),
            new OptionInstance.Enum<>(List.of(values()), StringRepresentable.fromEnum(GraphicsApi::values)),
            STARTUP,
            value -> EarlyVulkan.setGraphicsApi(value.name)
    );

    private final String name;

    GraphicsApi(String name) {
        this.name = name;
    }

    private static GraphicsApi byName(String name) {
        for (GraphicsApi api : values())
            if (api.name.equals(name))
                return api;
        return DEFAULT;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
