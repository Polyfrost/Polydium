package org.polyfrost.polydium.client.perf.gpuprofile;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

final class GpuProfileReport {
    private static final int TOP_ROWS = 80;
    static final String UNATTRIBUTED = "(unattributed)";
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private GpuProfileReport() {
    }

    record Row(String pass, String label, String namespace, double avgMs, double share, double maxMs, double callsPerFrame,
               double drawsPerFrame) {
    }

    static String write(GpuProfileSession session, List<String> labels, List<long[]> keyParts) throws IOException {
        int frames = Math.max(1, session.frames);

        double measuredMs = avgMs(session.measured);

        if (session.frames == 0)
            return String.format(Locale.ROOT, "GPU profile failed: no frame's GPU timestamps became available (%d recorded, %d skipped). "
                + "No report was written.", session.recordedFrames, session.skippedFrames);

        if (session.samples == 0)
            return String.format(Locale.ROOT, "GPU profile failed: %d frames read back, but no GPU call was timed in any of them. "
                + "No profiler hook saw a draw on this renderer. No report was written.", session.frames);

        if (measuredMs == 0 && percentileMs(session.gpuSpans, 1.0) == 0)
            return String.format(Locale.ROOT, "GPU profile failed: %d frames read back, but every GPU timestamp was zero (%s, %s). "
                + "This driver does not report usable GPU timings. No report was written.",
                session.frames, session.environment.get("gpu"), session.environment.get("driver"));

        List<Row> byKey = rows(session.byKey, frames, measuredMs, id -> labels.get((int) keyParts.get(id)[0]),
            id -> labels.get((int) keyParts.get(id)[1]), session.passLevel);
        List<Row> byPass = session.passGroups.isEmpty() ? rows(session.byPass, frames, measuredMs, labels::get, id -> "", false)
            : groupedPasses(session, frames, measuredMs, labels);
        List<Row> byMod = byNamespace(byKey, measuredMs);

        Path dir = FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("gpu-profiles");

        Files.createDirectories(dir);

        String stem = "gpu-" + LocalDateTime.now().format(FILE_TIME);
        Path text = dir.resolve(stem + ".txt");

        Files.writeString(text, text(session, frames, measuredMs, byPass, byKey, byMod), StandardCharsets.UTF_8);
        Files.writeString(dir.resolve(stem + ".json"), json(session, frames, measuredMs, byPass, byKey, byMod), StandardCharsets.UTF_8);

        StringBuilder summary = new StringBuilder();

        summary.append(String.format(Locale.ROOT, "GPU profile: %d frames, measured GPU work %.2f ms/frame, first-to-last stamp span %.2f ms "
            + "(p99 %.2f, includes GPU idle), present call time %.2f ms", session.frames, measuredMs, avgMs(session.gpuSpans),
            percentileMs(session.gpuSpans, 0.99), avgMs(session.presentWaits)));

        if (session.passLevel)
            summary.append("\nApple GPU: times are per render pass; per-pipeline rows are not per-draw.");

        for (int i = 0; i < Math.min(5, byPass.size()); i++) {
            Row row = byPass.get(i);

            summary.append(String.format(Locale.ROOT, "\n  %5.2f ms  %4.1f%%  %s", row.avgMs, row.share, row.pass));
        }

        summary.append("\nSaved to ").append(text.toAbsolutePath());

        return summary.toString();
    }

    interface Name {
        String of(int id);
    }

    static List<Row> rows(GpuProfileSession.Totals totals, int frames, double measuredMs, Name pass, Name label, boolean passLevel) {
        List<Row> rows = new ArrayList<>();

        for (int id = 0; id < totals.size(); id++) {
            if (totals.calls[id] == 0)
                continue;

            String labelName = label.of(id);
            double avg = totals.nanos[id] / 1e6 / frames;
            String namespace = passLevel && labelName.equals(GpuProfiler.PASS_BEGIN_NAME) ? UNATTRIBUTED : namespace(pass.of(id), labelName);

            rows.add(new Row(pass.of(id), labelName, namespace, avg, share(avg, measuredMs),
                totals.maxFrameNanos[id] / 1e6, (double) totals.calls[id] / frames, (double) totals.draws[id] / frames));
        }

        rows.sort(Comparator.comparingDouble(Row::avgMs).reversed());

        return rows;
    }

    static List<Row> groupedPasses(GpuProfileSession session, int frames, double measuredMs, List<String> labels) {
        GpuProfileSession.Totals totals = session.byPass;
        Map<Integer, List<Integer>> groups = new LinkedHashMap<>();

        for (int id = 0; id < totals.size(); id++) {
            if (totals.calls[id] != 0)
                groups.computeIfAbsent(session.passGroup(id), root -> new ArrayList<>()).add(id);
        }

        List<Row> rows = new ArrayList<>();

        groups.forEach((root, members) -> {
            long nanos = 0;
            long calls = 0;
            long draws = 0;
            StringJoiner name = new StringJoiner(" + ");

            members.sort(Comparator.comparing(id -> !id.equals(root)));

            for (int id : members) {
                nanos += totals.nanos[id];
                calls += totals.calls[id];
                draws += totals.draws[id];
                name.add(labels.get(id));
            }

            double avg = nanos / 1e6 / frames;

            rows.add(new Row(name.toString(), "", namespace(labels.get(root), ""), avg, share(avg, measuredMs),
                members.size() == 1 ? totals.maxFrameNanos[members.get(0)] / 1e6 : Double.NaN, (double) calls / frames, (double) draws / frames));
        });
        rows.sort(Comparator.comparingDouble(Row::avgMs).reversed());

        return rows;
    }

    private static List<Row> byNamespace(List<Row> byKey, double measuredMs) {
        Map<String, double[]> sums = new LinkedHashMap<>();

        for (Row row : byKey) {
            double[] sum = sums.computeIfAbsent(row.namespace, k -> new double[3]);

            sum[0] += row.avgMs;
            sum[1] += row.callsPerFrame;
            sum[2] += row.drawsPerFrame;
        }

        List<Row> rows = new ArrayList<>();

        sums.forEach((namespace, sum) -> rows.add(new Row("", "", namespace, sum[0], share(sum[0], measuredMs), Double.NaN, sum[1], sum[2])));
        rows.sort(Comparator.comparingDouble(Row::avgMs).reversed());

        return rows;
    }

    static String namespace(String pass, String label) {
        if (label.startsWith("shader "))
            label = label.substring("shader ".length());

        int colon = label.indexOf(':');

        if (colon > 0 && label.indexOf(' ') < 0)
            return label.substring(0, colon);

        colon = pass.indexOf(':');

        if (colon > 0 && pass.indexOf(' ') < 0)
            return pass.substring(0, colon);

        return pass.equals("command encoder") ? "command encoder" : "minecraft";
    }

    private static double share(double ms, double measuredMs) {
        return measuredMs <= 0 ? 0 : ms * 100.0 / measuredMs;
    }

    static double avgMs(LongArrayList values) {
        if (values.isEmpty())
            return 0;

        double sum = 0;

        for (int i = 0; i < values.size(); i++)
            sum += values.getLong(i);

        return sum / values.size() / 1e6;
    }

    static double percentileMs(LongArrayList values, double percentile) {
        if (values.isEmpty())
            return 0;

        long[] sorted = values.toLongArray();

        Arrays.sort(sorted);

        int index = (int) Math.min(sorted.length - 1, Math.ceil(percentile * sorted.length) - 1);

        return sorted[Math.max(0, index)] / 1e6;
    }

    private static String text(GpuProfileSession session, int frames, double measuredMs, List<Row> byPass, List<Row> byKey, List<Row> byMod) {
        StringBuilder out = new StringBuilder();

        out.append("Polydium GPU profile\n");
        out.append("====================\n");
        session.environment.forEach((key, value) -> out.append(String.format(Locale.ROOT, "%-12s %s%n", key, value)));
        out.append(String.format(Locale.ROOT, "%-12s %d s requested, %d frames read back (%d recorded, %d skipped, %d over %d samples)%n",
            "capture", session.request.seconds(), session.frames, session.recordedFrames, session.skippedFrames,
            session.overflowFrames, GpuProfiler.SAMPLES_PER_FRAME));
        out.append('\n');

        double cpuMs = avgMs(session.allCpuFrames);
        double spanMs = avgMs(session.gpuSpans);

        out.append("Frame                      avg      p50      p99      max\n");
        out.append(stats("CPU frame time", session.cpuFrames));
        out.append(stats("GPU frame span", session.gpuSpans));
        out.append(stats("GPU in measured calls", session.measured));
        out.append(stats("Present call time", session.presentWaits));
        out.append(String.format(Locale.ROOT, "%nPresent call time is wall time spent inside swapchain acquire and present (vkAcquireNextImageKHR,%n"
            + "vkQueuePresentKHR, or the GL buffer swap), per frame in the capture, skipped frames included. It covers vsync and%n"
            + "compositor waits, but also driver work and scheduling inside those calls.%n"));
        out.append("Presented on: ").append(presentThreads(session)).append('\n');
        out.append(String.format(Locale.ROOT, "Incomplete present waits: %d frames, whose present was still running %d frames later; "
            + "they count only the wait seen by then.%n", session.incompletePresentFrames, GpuProfiler.PRESENT_FRAMES));
        out.append(String.format(Locale.ROOT, "%nAverage FPS %.1f. Measured calls cover %.1f%% of the GPU span; the rest is GPU idle time between "
            + "submissions plus work no hook saw.%n%n", cpuMs > 0 ? 1000.0 / cpuMs : 0, spanMs > 0 ? measuredMs * 100 / spanMs : 0));

        table(out, "By render pass", byPass, false, Integer.MAX_VALUE);
        table(out, "By mod (pipeline namespace)", byMod, false, Integer.MAX_VALUE);
        table(out, "By render pass and pipeline", byKey, true, TOP_ROWS);

        out.append("Notes\n");
        out.append("- ms are averages per frame over every frame read back, including frames where the row did not run.\n");
        out.append("- % is the share of time spent in measured calls, not of the whole frame.\n");
        out.append("- Each call is bracketed by two timestamps. A timestamp is written once all earlier work has finished, but it does not\n");
        out.append("  hold back later work, so calls do not run in isolation. The next call can start inside a bracket; that part of it\n");
        out.append("  is charged to the earlier call, which then reads high while the next one reads low.\n");
        out.append("- A drawMultipleIndexed, multi-draw, indirect draw or Sodium multi-draw counts as one call; the draws column counts the\n");
        out.append("  draws inside it.\n");
        out.append("- Attachment clears run when a render pass begins and are charged to its \"" + GpuProfiler.PASS_BEGIN_NAME + "\" row. Clears the\n");
        out.append("  Vulkan backend defers land in that row of the pass they were folded into, or in a later clear.\n");

        if (session.passLevel) {
            out.append("- This GPU resolves timestamps only at Metal encoder boundaries, so each call is charged the time since the previous\n");
            out.append("  call ended. A render pass's \"" + GpuProfiler.PASS_BEGIN_NAME + "\" row carries the whole pass and its draws read about 0.\n");
            out.append("  Trust the render pass table. Idle GPU time lands in the next call.\n");
            out.append("- A pass's draws can come from any mod, so the by-mod table charges its \"" + GpuProfiler.PASS_BEGIN_NAME + "\" row\n");
            out.append("  to " + UNATTRIBUTED + ".\n");
            out.append("- Passes Polydium continued in one Vulkan render pass share one Metal encoder, whose time all lands in the first.\n");
            out.append("  The render pass table joins them into one row, named \"first + continued\", with no max ms.\n");
            out.append("  Joins are by name over the whole capture, so a row can also combine passes that never shared a render\n");
            out.append("  pass, only each shared one with a common pass at some point.\n");
        }

        return out.toString();
    }

    private static String presentThreads(GpuProfileSession session) {
        if (session.presentThreads.isEmpty())
            return "(no present seen)";

        StringBuilder out = new StringBuilder();

        session.presentThreads.forEach((thread, count) -> {
            if (out.length() > 0)
                out.append(", ");

            out.append(String.format(Locale.ROOT, "%s (%d frames)", thread, count));
        });

        return out.toString();
    }

    private static String stats(String name, LongArrayList values) {
        long max = 0;

        for (int i = 0; i < values.size(); i++)
            max = Math.max(max, values.getLong(i));

        return String.format(Locale.ROOT, "%-24s %6.2f   %6.2f   %6.2f   %6.2f ms%n", name, avgMs(values), percentileMs(values, 0.5),
            percentileMs(values, 0.99), max / 1e6);
    }

    private static void table(StringBuilder out, String title, List<Row> rows, boolean withLabel, int limit) {
        out.append(title).append('\n');
        out.append("    ms/frame   share   max ms  calls/f  draws/f  name\n");

        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            Row row = rows.get(i);
            String name = row.pass.isEmpty() ? row.namespace : withLabel ? row.pass + "  >  " + row.label : row.pass;

            out.append(String.format(Locale.ROOT, "    %8.3f  %5.1f%%  %7s  %7.1f  %7.1f  %s%n", row.avgMs, row.share,
                Double.isNaN(row.maxMs) ? "-" : String.format(Locale.ROOT, "%.2f", row.maxMs), row.callsPerFrame, row.drawsPerFrame, name));
        }

        if (rows.size() > limit)
            out.append(String.format(Locale.ROOT, "    ... %d more rows in the JSON report%n", rows.size() - limit));

        out.append('\n');
    }

    private static String json(GpuProfileSession session, int frames, double measuredMs, List<Row> byPass, List<Row> byKey, List<Row> byMod) {
        JsonObject root = new JsonObject();
        JsonObject env = new JsonObject();

        session.environment.forEach(env::addProperty);
        root.add("environment", env);
        root.addProperty("requestedSeconds", session.request.seconds());
        root.addProperty("frames", session.frames);
        root.addProperty("recordedFrames", session.recordedFrames);
        root.addProperty("skippedFrames", session.skippedFrames);
        root.addProperty("overflowFrames", session.overflowFrames);
        root.addProperty("samplesPerFrame", GpuProfiler.SAMPLES_PER_FRAME);
        root.add("cpuFrameMs", series(session.cpuFrames));
        root.add("gpuSpanMs", series(session.gpuSpans));
        root.add("measuredMs", series(session.measured));
        root.add("presentWaitMs", series(session.presentWaits));

        JsonObject threads = new JsonObject();

        session.presentThreads.forEach(threads::addProperty);
        root.add("presentThreads", threads);
        root.addProperty("incompletePresentFrames", session.incompletePresentFrames);
        root.addProperty("measuredAvgMs", measuredMs);
        root.add("byPass", rows(byPass));
        root.add("byMod", rows(byMod));
        root.add("byPassAndPipeline", rows(byKey));

        return new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create().toJson(root);
    }

    private static JsonObject series(LongArrayList values) {
        JsonObject stats = new JsonObject();

        stats.addProperty("avg", avgMs(values));
        stats.addProperty("p50", percentileMs(values, 0.5));
        stats.addProperty("p99", percentileMs(values, 0.99));
        stats.addProperty("max", percentileMs(values, 1.0));

        JsonArray all = new JsonArray();

        for (int i = 0; i < values.size(); i++)
            all.add(values.getLong(i) / 1e6);

        stats.add("frames", all);

        return stats;
    }

    private static JsonArray rows(List<Row> rows) {
        JsonArray array = new JsonArray();

        for (Row row : rows) {
            JsonObject object = new JsonObject();

            if (!row.pass.isEmpty())
                object.addProperty("pass", row.pass);

            if (!row.label.isEmpty())
                object.addProperty("pipeline", row.label);

            object.addProperty("namespace", row.namespace);
            object.addProperty("avgMs", row.avgMs);
            object.addProperty("share", row.share);

            if (!Double.isNaN(row.maxMs))
                object.addProperty("maxFrameMs", row.maxMs);

            object.addProperty("callsPerFrame", row.callsPerFrame);
            object.addProperty("drawsPerFrame", row.drawsPerFrame);
            array.add(object);
        }

        return array;
    }
}
