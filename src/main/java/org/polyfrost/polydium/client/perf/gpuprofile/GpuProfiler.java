package org.polyfrost.polydium.client.perf.gpuprofile;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GpuProfiler {
    static final int SLOTS = 4;
    static final int SAMPLES_PER_FRAME = 8192;
    private static final int QUERIES_PER_FRAME = SAMPLES_PER_FRAME * 2;

    static final int DISCARDED = -1;
    static final int CONTINUED = -1;

    public static boolean recording;

    private static @Nullable GpuStamps stamps;
    private static @Nullable GpuStamps abandoned;
    static @Nullable GpuProfileSession session;
    private static @Nullable Request request;
    private static boolean draining;
    private static int drainFrames;

    private static int slot;
    private static int cursor;
    private static int depth;
    private static boolean sampleOpen;
    private static boolean flushOpen;
    private static boolean resumeAfterFlush;
    private static int resumeKey;
    private static long frameStart;
    private static boolean frameTimed;
    private static final int[][] keys = new int[SLOTS][SAMPLES_PER_FRAME];
    private static final int[][] draws = new int[SLOTS][SAMPLES_PER_FRAME];
    private static final int[] counts = new int[SLOTS];
    private static final long[] cpuNanos = new long[SLOTS];
    private static final boolean[] overflowed = new boolean[SLOTS];
    private static final long[] readBuffer = new long[QUERIES_PER_FRAME];

    private static final Object2IntOpenHashMap<String> labelIds = new Object2IntOpenHashMap<>();
    private static final List<String> labelNames = new ArrayList<>();
    private static final Reference2IntOpenHashMap<Object> pipelineIds = new Reference2IntOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Object, String> shaderNames = new Reference2ObjectOpenHashMap<>();
    private static final Long2IntOpenHashMap keyIds = new Long2IntOpenHashMap();
    private static final List<long[]> keyParts = new ArrayList<>();

    private static volatile boolean timingPresent;
    static final int PRESENT_FRAMES = 8;
    private static final Object presentLock = new Object();
    private static long frameId;
    private static long nextPresentFrame;
    private static final long[] presentFrames = new long[PRESENT_FRAMES];
    private static final long[] presentWaitNanos = new long[PRESENT_FRAMES];
    private static final String[] presentThreads = new String[PRESENT_FRAMES];
    private static final boolean[] presentPending = new boolean[PRESENT_FRAMES];

    static {
        keyIds.defaultReturnValue(-1);
        pipelineIds.defaultReturnValue(-1);
        labelIds.defaultReturnValue(-1);
    }

    public static final int NO_PASS = label("(no render pass)");
    public static final int UNNAMED = label("(unnamed)");
    public static final int ENCODER = label("command encoder");
    public static final int OP_CLEAR = label("clear");
    public static final int OP_UPLOAD = label("upload");
    public static final int OP_COPY = label("copy");
    public static final int OP_PRESENT = label("present");
    static final String PASS_BEGIN_NAME = "(begin pass, attachment clears)";
    private static final int PASS_BEGIN = label(PASS_BEGIN_NAME);
    private static final int FIXED_LABELS = labelNames.size();

    private static int passLabel = NO_PASS;
    private static int encoderPass = NO_PASS;
    private static int drawLabel = UNNAMED;
    private static @Nullable String legacyLabel;

    private GpuProfiler() {
    }

    record Request(int seconds, Consumer<String> done) {
    }

    public static boolean start(int seconds, Consumer<String> done) {
        if (session != null || request != null)
            return false;

        request = new Request(seconds, done);

        return true;
    }

    public static boolean busy() {
        return session != null || request != null;
    }

    public static void frame() {
        if (session == null && request == null)
            return;

        frame(System.nanoTime());
    }

    static void frame(long now) {
        if (session == null) {
            Request started = request;

            request = null;

            if (!open(started))
                return;

            now = session.startNanos;
        }

        GpuProfileSession current = session;
        boolean presented;

        if (frameTimed)
            current.allCpuFrames.add(now - frameStart);

        frameTimed = false;

        synchronized (presentLock) {
            presented = takePresents(current, frameId, false);
            frameId++;
        }

        if (recording) {
            counts[slot] = cursor;
            cpuNanos[slot] = now - frameStart;
            current.recordedFrames++;
            slot = (slot + 1) % SLOTS;
        }

        recording = false;
        sampleOpen = false;
        flushOpen = false;
        depth = 0;
        cursor = 0;
        frameStart = now;

        boolean slotFree = collect(slot);

        if (!draining && now >= current.endNanos)
            draining = true;

        if (draining) {
            boolean collected = slotFree;

            for (int i = 1; i < SLOTS && collected; i++)
                collected = collect((slot + i) % SLOTS);

            if (collected && presented)
                finish(true);
            else if (++drainFrames > SLOTS * 8)
                finish(false);

            return;
        }

        synchronized (presentLock) {
            int index = (int) (frameId % PRESENT_FRAMES);

            if (presentFrames[index] >= 0) {
                takePresents(current, frameId - PRESENT_FRAMES, true);
                takePresents(current, frameId - 1, false);
            }

            presentFrames[index] = frameId;
            presentWaitNanos[index] = 0L;
            presentThreads[index] = null;
            presentPending[index] = false;
        }

        frameTimed = true;

        if (!slotFree) {
            current.skippedFrames++;

            return;
        }

        overflowed[slot] = false;
        recording = true;
    }

    private static boolean open(Request started) {
        Map<String, String> environment = GpuProfileBackend.describe();
        String unsupported = GpuProfileBackend.unsupported(environment);

        if (unsupported != null) {
            started.done().accept("Could not start the GPU profiler: " + unsupported);

            return false;
        }

        if (stamps == null) {
            try {
                stamps = GpuProfileBackend.createStamps(SLOTS * QUERIES_PER_FRAME);
            } catch (Throwable error) {
                started.done().accept("Could not start the GPU profiler: " + error);

                return false;
            }
        }

        return open(started, System.nanoTime(), environment, () -> stamps);
    }

    static boolean open(Request started, long now, Map<String, String> environment, Supplier<GpuStamps> create) {
        if (abandoned != null) {
            abandoned.close();
            abandoned = null;
        }

        try {
            if (stamps == null)
                stamps = create.get();
        } catch (Throwable error) {
            started.done().accept("Could not start the GPU profiler: " + error);

            return false;
        }

        boolean passLevel = GpuProfileBackend.passLevelOnly(environment);

        if (passLevel)
            environment.put("timing", "per render pass (Apple GPUs resolve timestamps at encoder boundaries)");

        synchronized (presentLock) {
            Arrays.fill(presentFrames, -1L);
            nextPresentFrame = frameId;
        }

        session = new GpuProfileSession(started, now, environment, passLevel);
        timingPresent = true;
        draining = false;
        drainFrames = 0;
        frameTimed = false;
        slot = 0;

        for (int i = 0; i < SLOTS; i++)
            counts[i] = -1;

        return true;
    }

    private static boolean takePresents(GpuProfileSession current, long last, boolean force) {
        for (; nextPresentFrame <= last; nextPresentFrame++) {
            int index = (int) (nextPresentFrame % PRESENT_FRAMES);

            if (presentFrames[index] != nextPresentFrame)
                continue;

            if (presentPending[index] && !force)
                return false;

            current.frameEnded(presentWaitNanos[index], presentThreads[index], presentPending[index]);
            presentFrames[index] = -1L;
        }

        return true;
    }

    private static boolean collect(int index) {
        int count = counts[index];

        if (count < 0)
            return true;

        GpuStamps active = stamps;
        int base = index * QUERIES_PER_FRAME;

        if (count > 0 && !active.read(base, count * 2, readBuffer))
            return false;

        session.addFrame(keys[index], draws[index], readBuffer, count, cpuNanos[index], overflowed[index], keyParts);

        if (count > 0)
            active.recycle(base, count * 2);

        counts[index] = -1;

        return true;
    }

    private static void finish(boolean drained) {
        GpuProfileSession done = session;

        session = null;
        recording = false;
        draining = false;
        timingPresent = false;

        synchronized (presentLock) {
            takePresents(done, frameId, true);
        }

        GpuStamps closing = stamps;

        stamps = null;

        if (drained)
            closing.close();
        else
            abandoned = closing;

        pipelineIds.clear();

        String summary;

        try {
            summary = GpuProfileReport.write(done, labelNames, keyParts);
        } catch (Throwable error) {
            summary = "GPU profile finished, but the report could not be written: " + error;
        }

        clearNames();
        done.request.done().accept(summary);
    }

    private static void clearNames() {
        labelNames.subList(FIXED_LABELS, labelNames.size()).clear();
        labelIds.clear();

        for (int i = 0; i < FIXED_LABELS; i++)
            labelIds.put(labelNames.get(i), i);

        keyIds.clear();
        keyParts.clear();
        shaderNames.clear();
        passLabel = NO_PASS;
        encoderPass = NO_PASS;
        drawLabel = UNNAMED;
    }

    public static void shutdown() {
        session = null;
        request = null;
        recording = false;
        draining = false;
        timingPresent = false;

        if (stamps != null) {
            stamps.close();
            stamps = null;
        }

        if (abandoned != null) {
            abandoned.close();
            abandoned = null;
        }

        pipelineIds.clear();
        clearNames();
    }

    public static long presentStart() {
        return timingPresent ? System.nanoTime() : 0L;
    }

    public static void presentWait(long startNanos, boolean present) {
        if (timingPresent)
            presentWait(startNanos, present, frameId);
    }

    public static long presentHandedOff() {
        if (!timingPresent)
            return -1L;

        synchronized (presentLock) {
            int index = (int) (frameId % PRESENT_FRAMES);

            if (presentFrames[index] == frameId)
                presentPending[index] = true;

            return frameId;
        }
    }

    public static void presentWait(long startNanos, boolean present, long frame) {
        if (!timingPresent || frame < 0 || startNanos == 0L)
            return;

        long waited = System.nanoTime() - startNanos;
        int index = (int) (frame % PRESENT_FRAMES);

        synchronized (presentLock) {
            if (presentFrames[index] != frame)
                return;

            presentWaitNanos[index] += waited;

            if (present) {
                presentThreads[index] = Thread.currentThread().getName();
                presentPending[index] = false;
            }
        }
    }

    public static int label(String name) {
        int id = labelIds.getInt(name);

        if (id < 0) {
            id = labelNames.size();
            labelNames.add(name);
            labelIds.put(name, id);
        }

        return id;
    }

    public static void pass(@Nullable Supplier<String> label) {
        String name = label == null ? null : label.get();

        passLabel = name == null ? UNNAMED : label(name);
        drawLabel = UNNAMED;
    }

    public static void pass(String label) {
        passLabel = label(label);
        drawLabel = UNNAMED;
    }

    public static void renamePass(String label) {
        passLabel = label(label);
    }

    public static long labels() {
        return ((long) passLabel << 32) | (drawLabel & 0xFFFFFFFFL);
    }

    public static void restoreLabels(long labels) {
        passLabel = (int) (labels >>> 32);
        drawLabel = (int) labels;
    }

    public static <T> void pipeline(T pipeline, Function<T, String> name) {
        int id = pipelineIds.getInt(pipeline);

        if (id < 0) {
            id = label(name.apply(pipeline));
            pipelineIds.put(pipeline, id);
        }

        drawLabel = id;
    }

    public static void legacyLabel(@Nullable String name) {
        legacyLabel = name;
    }

    public static @Nullable String legacyLabel() {
        return legacyLabel;
    }

    public static void shaderName(@Nullable Object program, String name) {
        if (program != null)
            shaderNames.putIfAbsent(program, name);
    }

    public static String shaderName(@Nullable Object program) {
        return program == null ? "(no shader)" : shaderNames.getOrDefault(program, "(unnamed shader)");
    }

    public static int key(int pass, int label) {
        long packed = ((long) pass << 32) | (label & 0xFFFFFFFFL);
        int id = keyIds.get(packed);

        if (id < 0) {
            id = keyParts.size();
            keyParts.add(new long[] {pass, label});
            keyIds.put(packed, id);
        }

        return id;
    }

    public static void beginDraw(@Nullable Object context, int drawCount) {
        begin(context, key(passLabel, drawLabel), drawCount);
    }

    public static void beginDraw(@Nullable Object context, String label, int drawCount) {
        begin(context, key(passLabel, label(label)), drawCount);
    }

    public static void beginPass(@Nullable Object context) {
        encoderPass = passLabel;
        begin(context, key(passLabel, PASS_BEGIN), 0);
    }

    public static void passContinued() {
        if (session.passLevel)
            session.mergePasses(encoderPass, passLabel);
    }

    public static void beginOp(@Nullable Object context, int op) {
        begin(context, key(ENCODER, op), 0);
    }

    public static void begin(@Nullable Object context, int key, int drawCount) {
        if (depth++ != 0 || !stamps.accepts(context))
            return;

        open(context, key, drawCount);
    }

    public static void end(@Nullable Object context) {
        if (depth == 0 || --depth != 0 || !sampleOpen)
            return;

        close(context);
    }

    public static void discard() {
        if (sampleOpen)
            keys[slot][cursor] = DISCARDED;
    }

    public static void beginFlush(@Nullable Object context, int op) {
        if (!recording || context == null || flushOpen || !stamps.accepts(context))
            return;

        resumeAfterFlush = sampleOpen;

        if (sampleOpen) {
            resumeKey = keys[slot][cursor];
            close(context);
        }

        open(context, key(ENCODER, op), 0);
        flushOpen = sampleOpen;
    }

    public static void endFlush(@Nullable Object context) {
        if (!flushOpen)
            return;

        flushOpen = false;
        close(context);

        if (resumeAfterFlush)
            open(context, resumeKey, CONTINUED);
    }

    private static void open(@Nullable Object context, int key, int drawCount) {
        if (cursor == SAMPLES_PER_FRAME) {
            overflowed[slot] = true;

            return;
        }

        stamps.write(slot * QUERIES_PER_FRAME + cursor * 2, context);
        keys[slot][cursor] = key;
        draws[slot][cursor] = drawCount;
        sampleOpen = true;
    }

    private static void close(@Nullable Object context) {
        sampleOpen = false;
        stamps.write(slot * QUERIES_PER_FRAME + cursor * 2 + 1, context);
        cursor++;
    }
}
