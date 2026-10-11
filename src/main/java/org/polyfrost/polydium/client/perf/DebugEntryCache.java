package org.polyfrost.polydium.client.perf;

//? if >= 1.21.10 {
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.client.gui.components.debug.DebugEntryFps;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

// Ported from Argentum with permission from rdh
public final class DebugEntryCache {
    private static final long INTERVAL_NANOS = Integer.getInteger("polydium.debugEntryCacheMs", 250) * 1_000_000L;

    private static final IdentityHashMap<DebugScreenEntry, Recording> RECORDINGS = new IdentityHashMap<>();

    private DebugEntryCache() {
    }

    public static boolean cacheable(DebugScreenEntry entry) {
        return !(entry instanceof DebugEntryFps) && entry.getClass().getName().startsWith("net.minecraft.");
    }

    public static boolean replay(DebugScreenEntry entry, DebugScreenDisplayer displayer, Object level, Object clientChunk, Object serverChunk, long now) {
        return replay(entry, displayer, level, clientChunk, serverChunk, now, INTERVAL_NANOS);
    }

    public static boolean replay(DebugScreenEntry entry, DebugScreenDisplayer displayer, Object level, Object clientChunk, Object serverChunk, long now, long interval) {
        Recording recording = RECORDINGS.get(entry);
        if (recording == null || now - recording.at >= interval
                || !recording.level.refersTo(level) || !recording.clientChunk.refersTo(clientChunk) || !recording.serverChunk.refersTo(serverChunk)) {
            return false;
        }
        recording.replay(displayer);
        return true;
    }

    public static DebugScreenDisplayer recorder(DebugScreenEntry entry, DebugScreenDisplayer displayer, Object level, Object clientChunk, Object serverChunk, long now) {
        Recording recording = new Recording(displayer, level, clientChunk, serverChunk, now);
        RECORDINGS.put(entry, recording);
        return recording;
    }

    public static void finish(DebugScreenDisplayer recorder) {
        ((Recording) recorder).target = null;
    }

    private static final class Recording implements DebugScreenDisplayer {
        private static final Object PRIORITY = new Object();
        private static final Object REGULAR = new Object();

        private final List<Object> lines = new ArrayList<>();
        private final WeakReference<Object> level;
        private final WeakReference<Object> clientChunk;
        private final WeakReference<Object> serverChunk;
        private final long at;
        private DebugScreenDisplayer target;

        Recording(DebugScreenDisplayer target, Object level, Object clientChunk, Object serverChunk, long at) {
            this.target = target;
            this.level = new WeakReference<>(level);
            this.clientChunk = new WeakReference<>(clientChunk);
            this.serverChunk = new WeakReference<>(serverChunk);
            this.at = at;
        }

        @Override
        public void addPriorityLine(String line) {
            this.lines.add(PRIORITY);
            this.lines.add(line);
            this.target.addPriorityLine(line);
        }

        @Override
        public void addLine(String line) {
            this.lines.add(REGULAR);
            this.lines.add(line);
            this.target.addLine(line);
        }

        @Override
        public void addToGroup(/*? if >= 1.21.11 {*/Identifier/*?} else {*//*ResourceLocation*//*?}*/ group, Collection<String> lines) {
            this.lines.add(group);
            this.lines.add(List.copyOf(lines));
            this.target.addToGroup(group, lines);
        }

        @Override
        public void addToGroup(/*? if >= 1.21.11 {*/Identifier/*?} else {*//*ResourceLocation*//*?}*/ group, String line) {
            this.lines.add(group);
            this.lines.add(line);
            this.target.addToGroup(group, line);
        }

        @SuppressWarnings("unchecked")
        void replay(DebugScreenDisplayer displayer) {
            for (int i = 0, size = this.lines.size(); i < size; i += 2) {
                Object kind = this.lines.get(i);
                Object value = this.lines.get(i + 1);
                if (kind == PRIORITY) {
                    displayer.addPriorityLine((String) value);
                } else if (kind == REGULAR) {
                    displayer.addLine((String) value);
                } else if (value instanceof String line) {
                    displayer.addToGroup((/*? if >= 1.21.11 {*/Identifier/*?} else {*//*ResourceLocation*//*?}*/) kind, line);
                } else {
                    displayer.addToGroup((/*? if >= 1.21.11 {*/Identifier/*?} else {*//*ResourceLocation*//*?}*/) kind, (Collection<String>) value);
                }
            }
        }
    }
}
//?}
