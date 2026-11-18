package org.polyfrost.polydium.early;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EarlyVulkan implements LanguageAdapter {

    private static final String KEY = "preferredGraphicsBackend:";

    private static final String MIXIN = "org.polyfrost.polydium.mixin.client.perf.vulkan.MixinAutoVulkan";

    //? if >= 26.2 {
    private static final String CLEANUP_MIXIN = "org.polyfrost.polydium.mixin.client.perf.vulkan.MixinKeepVulkanOptions";
    //?}

    public static final int BACKEND_FIX_VERSION = 4892;

    private static final Pattern DISCRETE_ARC = Pattern.compile("arc (pro )?(a\\d{2,3}|b[5-9]\\d{1,2})");

    private static final Pattern DISCRETE_RADEON = Pattern.compile("radeont? rx(?! ?vega)|radeont? rx vega( 56| 64|$)"
            + "|radeon vii|vega frontier|radeon (ai )?pro (wx? ?\\d{4}|r\\d{4})");

    private static final Pattern LAUNCH_LOG = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d+\\.log\\.gz");

    private static final Pattern STACK_FRAME = Pattern.compile("^\\s+at (?:\\S*//)?([\\w.$]+)\\.([\\w$<>]+)\\(");

    private static Boolean vulkanAtStartup;

    //? if >= 26.2 {
    private static List<String> createdOptions;

    public static boolean vanillaOpenGl;

    private static boolean irisLoaded = FabricLoader.getInstance().isModLoaded("iris");
    //?}

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

    // "" = auto-switched, first launch not checked yet; "no" = not switched
    // "checked" = checked, so a default backend is the user's own choice
    // "vulkan-failed" = a Vulkan start failed and the backend was reset, not retried until the user picks Vulkan
    // "failed" = the same, written by builds that counted Sodium blocking Vulkan as a failure
    // "done" = checked, but vanilla's backend datafix may have reset it, so 26.2+ retries once
    // "sodium" = a failure that may have been Sodium blocking Vulkan or a crash outside rendering, decided again
    // once like a fresh install unless the user picks a backend first
    public static boolean switched() {
        String state = markerState();
        return state != null && !state.equals("no");
    }

    public static boolean switchPending() {
        return "".equals(markerState());
    }

    public static boolean failed() {
        String state = markerState();
        return "vulkan-failed".equals(state) || "failed".equals(state);
    }

    private static boolean sodiumRetryPending() {
        return "sodium".equals(markerState());
    }

    public static void finishSwitch() {
        try {
            Files.writeString(marker(), "checked");
        } catch (IOException ignored) {
        }
    }

    public static void markFailed(String reason) {
        LoggerFactory.getLogger("polydium/vulkan").warn("Resetting graphics API to Default: {}", reason);
        try {
            Files.createDirectories(marker().getParent());
            Files.writeString(marker(), "vulkan-failed");
            Files.writeString(marker().resolveSibling("iris-retry"), "");
            Files.writeString(crashRecheckFile(), "");
            Files.writeString(marker().resolveSibling("crash-recheck"), "");
        } catch (IOException ignored) {
        }
    }

    private static Path crashRecheckFile() {
        return marker().resolveSibling("crash-recheck-2");
    }

    private static Path sodiumRetryFile() {
        return marker().resolveSibling("sodium-retry");
    }

    public static void retrySodiumBlockedFailure() {
        try {
            if (!"failed".equals(markerState()) || Files.exists(sodiumRetryFile()) || !graphicsApiSaved() || !"default".equals(graphicsApi()))
                return;
            Instant failedAt = Files.getLastModifiedTime(marker()).toInstant();
            Instant apiAt = Files.getLastModifiedTime(graphicsApiFile()).toInstant();
            if (apiAt.isBefore(failedAt.minus(Duration.ofSeconds(2))) || apiAt.isAfter(failedAt.plus(Duration.ofSeconds(5))))
                return;
            Files.writeString(marker(), "sodium");
            Files.writeString(sodiumRetryFile(), "");
        } catch (IOException ignored) {
        }
    }

    private static Path discreteGpuFile() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("discrete-gpu");
    }

    public static boolean discreteGpuKnown() {
        try {
            return Files.getLastModifiedTime(discreteGpuFile()).toInstant().isAfter(Instant.now().minus(Duration.ofDays(30)));
        } catch (IOException e) {
            return false;
        }
    }

    public static void recordDiscreteGpu(boolean discrete) {
        try {
            Files.createDirectories(discreteGpuFile().getParent());
            Files.writeString(discreteGpuFile(), Boolean.toString(discrete));
        } catch (IOException ignored) {
        }
    }

    private static Boolean discreteGpu() {
        if (!discreteGpuKnown())
            return null;
        try {
            return Boolean.valueOf(Files.readString(discreteGpuFile()).strip());
        } catch (IOException e) {
            return null;
        }
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

    public static boolean graphicsApiSaved() {
        return Files.exists(graphicsApiFile());
    }

    public static void setGraphicsApi(String api) {
        try {
            Files.createDirectories(graphicsApiFile().getParent());
            Files.writeString(graphicsApiFile(), api);
        } catch (IOException ignored) {
            return;
        }
        // Vulkan can only be written after a failure if the user chose it, and a new failure records itself again.
        if (("vulkan".equals(api) && failed()) || sodiumRetryPending())
            finishSwitch();
    }

    public static boolean renderCrash(Throwable t) {
        for (int depth = 0; t != null && depth < 32; t = t.getCause(), depth++)
            for (StackTraceElement frame : t.getStackTrace()) {
                if (eventPump(frame.getClassName(), frame.getMethodName()))
                    break;
                if (renderFrame(frame.getClassName()))
                    return true;
            }
        return false;
    }

    public static boolean renderCrashReport(List<String> report) {
        boolean belowPump = false;
        for (String line : report) {
            if (line.startsWith("A detailed walkthrough"))
                break;
            if (line.startsWith("Caused by:")) {
                belowPump = false;
                continue;
            }
            Matcher frame = STACK_FRAME.matcher(line);
            if (belowPump || !frame.find())
                continue;
            if (eventPump(frame.group(1), frame.group(2)))
                belowPump = true;
            else if (renderFrame(frame.group(1)))
                return true;
        }
        return false;
    }

    private static boolean renderFrame(String className) {
        String cls = className.toLowerCase(Locale.ROOT);
        return cls.contains("vulkan") || cls.contains("blaze3d") || cls.contains("renderpearl");
    }

    private static boolean eventPump(String className, String method) {
        return className.startsWith("org.lwjgl.sdl.SDLEvents")
                || className.equals("org.lwjgl.glfw.GLFW") && method.contains("Events");
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
            boolean unclean = false;
            boolean startFailed = false;
            try {
                Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
                boolean exists = Files.exists(options);
                List<String> lines = exists ? Files.readAllLines(options) : packOptions();
                unclean = lines.contains("startedCleanly:false");
                startFailed = unclean && exists && !lines.equals(packOptions());
            } catch (IOException ignored) {
            }
            if (vulkan && startFailed) {
                markFailed("last game startup did not finish");
                setGraphicsApi("default");
            }
            vulkanAtStartup = vulkan && !unclean;
        }
        return vulkanAtStartup;
    }

    private static List<String> modIds() {
        List<String> ids = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ids.add(mod.getMetadata().getId());
            ids.addAll(mod.getMetadata().getProvides());
        }
        return ids;
    }

    public static Path firstCopied(Path configDir, List<String> modIds, String file) {
        for (String id : modIds) {
            Path copied = switch (id) {
                case "yosbr" -> configDir.resolve("yosbr").resolve(file);
                case "config_manager" -> configDir.resolve("modpack_defaults").resolve(file);
                default -> null;
            };
            if (copied != null && Files.exists(copied))
                return copied;
        }
        return null;
    }

    private static List<String> packOptions() throws IOException {
        FabricLoader loader = FabricLoader.getInstance();
        Path copied = firstCopied(loader.getConfigDir(), modIds(), "options.txt");
        if (copied != null)
            return Files.readAllLines(copied);
        if (!loader.isModLoaded("defaultoptions"))
            return List.of();
        Path defaults = loader.getConfigDir().resolve("defaultoptions");
        Path extraOptions = defaults.resolve("extra").resolve("options.txt");
        if (Files.exists(extraOptions))
            return Files.readAllLines(extraOptions);
        Path defaultOptions = defaults.resolve("options.txt");
        if (Files.exists(defaultOptions))
            return Files.readAllLines(defaultOptions).stream().filter(line -> !line.startsWith("key_")).toList();
        return List.of();
    }

    //? if >= 1.21.11 {
    private static boolean mixinDisabled(String mixin) {
        for (String fragment : System.getProperty("polydium.disable", "").split(","))
            if (!fragment.isBlank() && mixin.contains(fragment.trim()))
                return true;
        return false;
    }

    private static void carryPackGraphicsApi() throws IOException {
        if (Files.exists(graphicsApiFile()))
            return;
        FabricLoader loader = FabricLoader.getInstance();
        Path config = loader.getConfigDir();
        List<String> mods = modIds();
        Path pack = firstCopied(config, mods, "polydium/graphics-api");
        if (pack == null && loader.isModLoaded("defaultoptions")) {
            String extra = "defaultoptions/extra/polydium/graphics-api";
            pack = Files.exists(config.resolve(extra)) ? config.resolve(extra) : firstCopied(config, mods, "config/" + extra);
        }
        if (pack == null)
            return;
        Files.createDirectories(graphicsApiFile().getParent());
        Files.copy(pack, graphicsApiFile());
    }

    private static void preferVulkan() throws IOException {
        if (mixinDisabled(MIXIN))
            return;
        carryPackGraphicsApi();
        cleanNonRenderStartupCrash();
        recheckStartupFailure();

        //? if >= 26.2 {
        boolean irisRetry = irisRetryDue();
        boolean retry = irisRetry || "done".equals(markerState()) || sodiumRetryPending();
        //?} else {
        /*boolean retry = sodiumRetryPending();
        *///?}
        boolean decided = !retry && Files.exists(marker()) && (switched() || (!Boolean.TRUE.equals(vulkanEligible())
                && Files.getLastModifiedTime(marker()).toInstant().isAfter(Instant.now().minus(Duration.ofDays(30)))));
        //? if < 26.2 {
        /*if (decided)
            return;
        *///?}

        Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
        //? if >= 26.2 {
        if (Files.isSymbolicLink(options) && !Files.exists(options))
            return;
        //?}
        List<String> lines = new ArrayList<>();
        if (Files.exists(options)) {
            options = options.toRealPath();
            lines.addAll(Files.readAllLines(options));
        }
        List<String> pack = packOptions();
        //? if >= 26.2 {
        if (!Files.exists(options) && pack.isEmpty() && mixinDisabled(CLEANUP_MIXIN))
            return;
        boolean saved = !lines.isEmpty() && !lines.equals(pack);
        //?}
        if (!Files.exists(options))
            lines.addAll(pack);

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
        String api = graphicsApi();
        boolean apiUsable = !("vulkan".equals(api) && failed());
        boolean backendInFile = index >= 0 && !lines.get(index).contains("default");
        boolean savedOverPack = apiUsable && backendInFile && !Files.exists(options) && Files.exists(graphicsApiFile());
        if (!savedOverPack && !lines.isEmpty() && version < BACKEND_FIX_VERSION)
            return;
        if (savedOverPack || (apiUsable && (!saved || switchPending()) && !backendInFile && !"default".equals(api))) {
            writeBackend(options, lines, index, api);
            if (retry)
                finishSwitch();
            return;
        }
        if (backendInFile) {
            if (retry)
                finishSwitch();
            if (!Files.exists(options))
                writeOptions(options, lines);
            return;
        }
        if (decided)
            return;
        //?} else {
        /*if (!"default".equals(graphicsApi()))
            return;
        *///?}

        // oshi can't load this early, so an unknown GPU waits for EarlyVulkanPreLaunch. Checked before the marker
        // changes, so nothing here is lost when that run decides.
        Boolean vulkan = vulkanEligible();
        if (vulkan == null)
            return;
        if (retry)
            finishSwitch();

        Files.createDirectories(marker().getParent());
        Files.writeString(marker(), vulkan ? "" : "no");
        if (!vulkan)
            return;
        //? if >= 26.2 {
        if (irisRetry)
            Files.writeString(irisRetryFile(), "");
        //?}

        //? if < 26.2 {
        /*setGraphicsApi("vulkan");
        *///?} else {
        writeBackend(options, lines, index, "vulkan");
        //?}
    }

    //? if >= 26.2 {
    private static void writeBackend(Path options, List<String> lines, int index, String api) throws IOException {
        boolean created = lines.isEmpty() && !Files.exists(options);
        if (lines.isEmpty())
            lines.add("version:" + BACKEND_FIX_VERSION);
        if (index >= 0)
            lines.remove(index);
        lines.add(KEY + '"' + api + '"');
        writeOptions(options, lines);
        if (created)
            createdOptions = List.copyOf(lines);
    }

    private static void writeOptions(Path options, List<String> lines) throws IOException {
        Path temp = options.resolveSibling("options.txt.polydium");
        try {
            Files.write(temp, lines);
            Files.move(temp, options, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static Path irisRetryFile() {
        return FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("iris-retry");
    }

    private static boolean irisRetryDue() {
        return failed() && irisLoaded && !Files.exists(irisRetryFile()) && !irisShadersOn();
    }

    public static void removeCreatedOptions() {
        List<String> created = createdOptions;
        if (created == null)
            return;
        createdOptions = null;
        try {
            Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
            // Any other content came from another mod during startup
            if (created.equals(Files.readAllLines(options)))
                Files.delete(options);
        } catch (IOException ignored) {
        }
    }

    //?}

    public static Boolean vulkanEligible() {
        String forced = System.getProperty("polydium.vulkanEligible");
        if (forced != null)
            return Boolean.parseBoolean(forced);
        boolean appleArm = System.getProperty("os.name", "").startsWith("Mac") && "aarch64".equals(System.getProperty("os.arch"));
        return appleArm ? Boolean.TRUE : discreteGpu();
    }

    private static boolean irisShadersOn() {
        Properties iris = new Properties();
        try (InputStream in = Files.newInputStream(FabricLoader.getInstance().getConfigDir().resolve("iris.properties"))) {
            iris.load(in);
        } catch (IOException e) {
            return false;
        }
        String pack = iris.getProperty("shaderPack", "");
        return !"false".equals(iris.getProperty("enableShaders")) && !pack.isEmpty() && !"(internal)".equals(pack);
    }

    private static void cleanNonRenderStartupCrash() throws IOException {
        Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
        Instant startedAt = modified(options);
        if (startedAt == null || !Files.readAllLines(options).contains("startedCleanly:false"))
            return;
        Path report = newestReport(startedAt, Instant.MAX);
        if (report != null && nonRenderStartupCrash(report, Instant.MAX))
            markStartClean();
    }

    private static void recheckStartupFailure() throws IOException {
        Path done = crashRecheckFile();
        boolean irisShaders = FabricLoader.getInstance().isModLoaded("iris") && irisShadersOn();
        if (!failed() || !"default".equals(graphicsApi()) || Files.exists(done) || irisShaders)
            return;
        Files.writeString(done, "");
        Instant failedAt = Files.getLastModifiedTime(marker()).toInstant();
        Instant legacyRecheck = modified(marker().resolveSibling("crash-recheck"));
        if (legacyRecheck != null && Duration.between(failedAt, legacyRecheck).abs().compareTo(Duration.ofSeconds(5)) < 0)
            return;
        Path report = newestReport(Instant.MIN, failedAt);
        if (report == null)
            return;
        Instant from = modified(report);
        Instant launchedAt = ProcessHandle.current().info().startInstant().orElse(Instant.MAX);
        Instant rotatedBy = Collections.min(List.of(failedAt.plus(Duration.ofSeconds(5)), launchedAt));
        long launches = files(logDirs(), "", ".log.gz").stream()
                .filter(file -> LAUNCH_LOG.matcher(file.getFileName().toString()).matches())
                .map(EarlyVulkan::modified)
                .filter(at -> at != null && at.isAfter(from) && at.isBefore(rotatedBy)).count();
        if (launches != 1 || !nonRenderStartupCrash(report, failedAt))
            return;
        LoggerFactory.getLogger("polydium/vulkan").info("Retrying Vulkan: {} was not a render crash", report.getFileName());
        Files.writeString(marker(), "sodium");
    }

    private static Path newestReport(Instant after, Instant before) {
        Path report = null;
        Instant reportAt = after;
        for (Path file : files(crashDirs(), "crash-", "-client.txt")) {
            Instant at = modified(file);
            if (at != null && at.isBefore(before) && at.isAfter(reportAt)) {
                report = file;
                reportAt = at;
            }
        }
        return report;
    }

    private static boolean nonRenderStartupCrash(Path report, Instant until) throws IOException {
        Instant reportAt = modified(report);
        if (reportAt == null || files(crashDirs(), "hs_err_pid", ".log").stream().map(EarlyVulkan::modified)
                .anyMatch(at -> at != null && at.isAfter(reportAt) && at.isBefore(until)))
            return false;
        List<String> lines = Files.readAllLines(report);
        int walkthrough = lines.indexOf("A detailed walkthrough of the error, its code path and all known details is as follows:");
        if (walkthrough < 0 || lines.subList(0, walkthrough).stream().noneMatch(line -> STACK_FRAME.matcher(line).find()))
            return false;
        boolean startup = lines.stream().anyMatch(line -> line.equals("Description: Initializing game") || line.contains("net.minecraft.client.Minecraft.<init>"));
        return startup && !renderCrashReport(lines);
    }

    private static List<Path> files(Set<Path> dirs, String prefix, String suffix) {
        List<Path> found = new ArrayList<>();
        for (Path dir : dirs)
            try (var files = Files.list(dir)) {
                files.filter(file -> {
                    String name = file.getFileName().toString();
                    return name.startsWith(prefix) && name.endsWith(suffix);
                }).forEach(found::add);
            } catch (IOException | RuntimeException ignored) {
            }
        return found;
    }

    private static Instant modified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException e) {
            return null;
        }
    }

    private static Set<Path> logDirs() {
        Set<Path> dirs = new LinkedHashSet<>();
        for (Path root : List.of(FabricLoader.getInstance().getGameDir(), Path.of("")))
            dirs.add(root.toAbsolutePath().normalize().resolve("logs"));
        return dirs;
    }

    private static Set<Path> crashDirs() {
        Set<Path> dirs = new LinkedHashSet<>();
        for (Path root : List.of(FabricLoader.getInstance().getGameDir(), Path.of(""))) {
            root = root.toAbsolutePath().normalize();
            dirs.add(root.resolve("crash-reports"));
            dirs.add(root);
        }
        return dirs;
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
