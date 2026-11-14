package org.polyfrost.polydium.client.perf.gpuprofile;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

//? if >= 26.1 {
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
//?} else {
/*import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
*///?}

public final class GpuProfileCommand {
    private static final int DEFAULT_SECONDS = 10;
    private static final int MAX_SECONDS = 120;

    private GpuProfileCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
            literal("polydium").then(literal("gpuprofile")
                .executes(command -> run(command.getSource(), DEFAULT_SECONDS))
                .then(argument("seconds", IntegerArgumentType.integer(1, MAX_SECONDS))
                    .executes(command -> run(command.getSource(), IntegerArgumentType.getInteger(command, "seconds")))))));
    }

    private static int run(FabricClientCommandSource source, int seconds) {
        boolean started = GpuProfiler.start(seconds, summary -> {
            source.sendFeedback(Component.literal(summary));
            Minecraft.getInstance().keyboardHandler.setClipboard(summary);
        });

        if (!started) {
            source.sendError(Component.literal("A GPU profile is already being recorded"));

            return 0;
        }

        source.sendFeedback(Component.literal("Recording GPU timings for " + seconds + " s. Keep playing normally."));

        return 1;
    }
}
