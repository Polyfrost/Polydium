package org.polyfrost.polydium.early;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;

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

    public EarlyVulkan() {
        //? if >= 26.2 {
        try {
            preferVulkan();
        } catch (Throwable ignored) {
        }
        //?}
    }

    private static Path marker() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("auto-vulkan");
    }

    public static boolean switched() {
        try {
            return Files.readString(marker()).isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    //? if >= 26.2 {
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
        if (version < BACKEND_FIX_VERSION || index >= 0 && !lines.get(index).contains("default"))
            return;

        boolean appleArm = System.getProperty("os.name", "").startsWith("Mac") && "aarch64".equals(System.getProperty("os.arch"));
        boolean vulkan = appleArm || hasDiscreteGpu();

        Files.createDirectories(marker().getParent());
        Files.writeString(marker(), vulkan ? "" : "no");
        if (!vulkan)
            return;

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
