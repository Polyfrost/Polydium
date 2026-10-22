package org.polyfrost.polydium.early;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class EarlyVulkan implements LanguageAdapter {

    private static final String KEY = "preferredGraphicsBackend:";

    private static final String MIXIN = "org.polyfrost.polydium.mixin.client.perf.vulkan.MixinAutoVulkan";

    private static final int BACKEND_FIX_VERSION = 4892;

    private static final Pattern DISCRETE_ARC = Pattern.compile("arc (pro )?(a\\d{2,3}|b[5-9]\\d{1,2})");

    private static final Pattern DISCRETE_RADEON = Pattern.compile("radeont? rx(?! ?vega)|radeont? rx vega( 56| 64|$)"
            + "|radeon vii|vega frontier|radeon (ai )?pro (wx? ?\\d{4}|r\\d{4})");

    private static Boolean vulkanAtStartup;

    public EarlyVulkan() {
        //? if >= 1.21.11 {
        try {
            preferVulkan();
        } catch (Throwable ignored) {
        }
        //?}
    }

    private static Path marker() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("auto-vulkan");
    }

    private static String markerState() {
        try {
            return Files.readString(marker());
        } catch (IOException e) {
            return null;
        }
    }

    // "" = auto-switched, first launch not checked yet; "done" = checked; "no" = not switched
    public static boolean switched() {
        String state = markerState();
        return state != null && !state.equals("no");
    }

    public static boolean switchPending() {
        return "".equals(markerState());
    }

    public static void finishSwitch() {
        try {
            Files.writeString(marker(), "done");
        } catch (IOException ignored) {
        }
    }

    public static void logReset(String reason) {
        LoggerFactory.getLogger("polydium/vulkan").warn("Resetting graphics API to Default: {}", reason);
    }

    private static Path graphicsApiFile() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("graphics-api");
    }

    public static String graphicsApi() {
        try {
            return Files.readString(graphicsApiFile()).strip();
        } catch (IOException e) {
            return "default";
        }
    }

    public static void setGraphicsApi(String api) {
        try {
            Files.createDirectories(graphicsApiFile().getParent());
            Files.writeString(graphicsApiFile(), api);
        } catch (IOException ignored) {
        }
    }

    public static boolean renderCrash(Throwable t) {
        for (int depth = 0; t != null && depth < 32; t = t.getCause(), depth++)
            for (StackTraceElement frame : t.getStackTrace()) {
                String cls = frame.getClassName().toLowerCase(Locale.ROOT);
                if (cls.contains("vulkan") || cls.contains("blaze3d") || cls.contains("renderpearl"))
                    return true;
            }
        return false;
    }

    public static void markStartClean() {
        try {
            Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
            List<String> lines = Files.readAllLines(options);
            int index = lines.indexOf("startedCleanly:false");
            if (index < 0)
                return;
            lines.set(index, "startedCleanly:true");
            Files.write(options, lines);
        } catch (Throwable ignored) {
        }
    }

    public static boolean vulkanAtStartup() {
        if (vulkanAtStartup == null) {
            boolean vulkan = "vulkan".equals(graphicsApi());
            boolean clean = true;
            try {
                clean = !Files.readAllLines(FabricLoader.getInstance().getGameDir().resolve("options.txt")).contains("startedCleanly:false");
            } catch (IOException ignored) {
            }
            if (vulkan && !clean) {
                logReset("last game startup did not finish");
                setGraphicsApi("default");
            }
            vulkanAtStartup = vulkan && clean;
        }
        return vulkanAtStartup;
    }

    //? if >= 1.21.11 {
    private static void preferVulkan() throws IOException {
        if (Files.exists(marker()) && (switched()
                || Files.getLastModifiedTime(marker()).toInstant().isAfter(Instant.now().minus(Duration.ofDays(30)))))
            return;
        for (String fragment : System.getProperty("polydium.disable", "").split(","))
            if (!fragment.isBlank() && MIXIN.contains(fragment.trim()))
                return;

        Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
        if (!Files.exists(options))
            return;
        options = options.toRealPath();
        List<String> lines = new ArrayList<>(Files.readAllLines(options));

        int version = 0;
        int index = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("version:"))
                version = Integer.parseInt(line.substring("version:".length()).trim());
            else if (line.startsWith(KEY))
                index = i;
            else if (line.trim().equals("startedCleanly:false"))
                return;
        }
        //? if >= 26.2 {
        if (version < BACKEND_FIX_VERSION || index >= 0 && !lines.get(index).contains("default"))
            return;
        //?} else {
        /*if (!"default".equals(graphicsApi()))
            return;
        *///?}

        boolean appleArm = System.getProperty("os.name", "").startsWith("Mac") && "aarch64".equals(System.getProperty("os.arch"));
        boolean vulkan = appleArm || hasDiscreteGpu();

        Files.createDirectories(marker().getParent());
        Files.writeString(marker(), vulkan ? "" : "no");
        if (!vulkan)
            return;

        //? if < 26.2 {
        /*setGraphicsApi("vulkan");
        *///?} else {
        if (index >= 0)
            lines.remove(index);
        lines.add(KEY + "\"vulkan\"");
        Path temp = options.resolveSibling("options.txt.polydium");
        try {
            Files.write(temp, lines);
            Files.move(temp, options, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
        //?}
    }

    private static boolean hasDiscreteGpu() {
        try {
            for (oshi.hardware.GraphicsCard card : new oshi.SystemInfo().getHardware().getGraphicsCards())
                if (isDiscreteName(card.getVendor() + " " + card.getName()))
                    return true;
        } catch (Throwable ignored) {
        }
        return false;
    }
    //?}

    public static boolean isDiscreteName(String gpu) {
        String name = gpu.toLowerCase(Locale.ROOT).replaceAll("\\((tm|r)\\)", " ").replaceAll("\\s+", " ").strip();
        return name.contains("nvidia") || name.contains("geforce")
                || DISCRETE_RADEON.matcher(name).find() || DISCRETE_ARC.matcher(name).find();
    }

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) {
        throw new UnsupportedOperationException("polydium-early is not a language adapter");
    }
}
