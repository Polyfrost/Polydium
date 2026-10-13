package org.polyfrost.polydium.client.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class RenderPassStats {

    public static final boolean ENABLED = Boolean.getBoolean("polydium.passStats");

    private static final Logger LOGGER       = LoggerFactory.getLogger("polydium/perf");
    private static final long   REPORT_NANOS = 5_000_000_000L;
    private static final int    TOP          = 15;

    private static final Map<String, int[]> SPLITS = new HashMap<>();

    private static long lastReport = System.nanoTime();
    private static int  submits;
    private static int  begun;
    private static int  merged;

    private RenderPassStats() {
    }

    public static void begun() {
        begun++;
    }

    public static void merged() {
        merged++;
    }

    public static void split(Supplier<String> passLabel) {
        String cause = StackWalker.getInstance().walk(frames -> {
            String command = null;

            for (StackWalker.StackFrame frame : (Iterable<StackWalker.StackFrame>) frames::iterator) {
                String type   = frame.getClassName();
                String method = frame.getMethodName();

                if (method.contains("polydium$") || type.endsWith("RenderPassStats"))
                    continue;

                if (type.endsWith("VulkanCommandEncoder")) {
                    if (!method.equals("commandBuffer") && !method.equals("memoryBarrier"))
                        command = method;

                    continue;
                }

                if (!type.startsWith("com.mojang."))
                    return command + " <- " + simpleName(type) + "." + method + ":" + frame.getLineNumber();
            }

            return command + " <- ?";
        });

        count(cause + " after '" + passLabel.get() + "'");
    }

    public static void refused(Supplier<String> previous, Supplier<String> next, String reason) {
        count("refused (" + reason + "): '" + previous.get() + "' -> '" + next.get() + "'");
    }

    public static void submit() {
        submits++;

        long now = System.nanoTime();
        if (now - lastReport < REPORT_NANOS)
            return;

        StringBuilder report = new StringBuilder(String.format("Vulkan passes per submit: %.1f begun, %.1f merged, over %d submits", (double) begun / submits, (double) merged / submits, submits));

        SPLITS.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .limit(TOP)
                .forEach(entry -> report.append(String.format("%n  %6.2f/submit  %s", (double) entry.getValue()[0] / submits, entry.getKey())));

        LOGGER.info(report.toString());
        SPLITS.clear();

        lastReport = now;
        submits    = 0;
        begun      = 0;
        merged     = 0;
    }

    private static void count(String key) {
        SPLITS.computeIfAbsent(key, ignored -> new int[1])[0]++;
    }

    private static String simpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
