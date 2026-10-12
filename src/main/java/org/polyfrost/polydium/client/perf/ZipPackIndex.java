package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipPackIndex {
    public static final boolean AVAILABLE = !FabricLoader.getInstance().isModLoaded("quick-pack");

    private static final Map<ZipFile, ZipPackIndex> INDEXES = Collections.synchronizedMap(new WeakHashMap<>());

    private final ZipEntry[] entries;
    private final String[] names;
    private final int[] ordinals;

    private ZipPackIndex(ZipFile zipFile) {
        List<ZipEntry> files = new ArrayList<>();
        Enumeration<? extends ZipEntry> all = zipFile.entries();
        while (all.hasMoreElements()) {
            ZipEntry entry = all.nextElement();
            if (!entry.isDirectory()) {
                files.add(entry);
            }
        }
        this.entries = files.toArray(new ZipEntry[0]);
        Integer[] order = new Integer[this.entries.length];
        Arrays.setAll(order, i -> i);
        Arrays.sort(order, Comparator.comparing(i -> this.entries[i].getName()));
        this.names = new String[order.length];
        this.ordinals = new int[order.length];
        for (int i = 0; i < order.length; i++) {
            this.names[i] = this.entries[order[i]].getName();
            this.ordinals[i] = order[i];
        }
    }

    public static Enumeration<? extends ZipEntry> entries(ZipFile zipFile, String prefix) {
        return INDEXES.computeIfAbsent(zipFile, ZipPackIndex::new).withPrefix(prefix);
    }

    private Enumeration<ZipEntry> withPrefix(String prefix) {
        int from = Arrays.binarySearch(this.names, prefix);
        if (from < 0) {
            from = -from - 1;
        } else {
            while (from > 0 && this.names[from - 1].equals(prefix)) {
                from--;
            }
        }
        int to = from;
        while (to < this.names.length && this.names[to].startsWith(prefix)) {
            to++;
        }
        int[] matched = Arrays.copyOfRange(this.ordinals, from, to);
        Arrays.sort(matched);
        List<ZipEntry> result = new ArrayList<>(matched.length);
        for (int ordinal : matched) {
            result.add(this.entries[ordinal]);
        }
        return Collections.enumeration(result);
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("polydium-zipindex", ".zip");
        String[] paths = {"assets/", "assets/b/textures/z.png", "assets/a/textures/", "assets/a/textures/x.png", "pack.mcmeta",
            "assets/a/textures/Bad Name.png", "assets/a/textures2/y.png", "assets/a/textures/sub/￿.png", "assets/a/textures", "assets/a/textures/a.png"};
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file))) {
            for (String path : paths) {
                out.putNextEntry(new ZipEntry(path));
                out.closeEntry();
            }
        }
        try (ZipFile zipFile = new ZipFile(file.toFile())) {
            for (String prefix : new String[] {"assets/a/textures/", "assets/a/", "assets/c/", "", "assets/a/textures/sub/", "pack.mcmeta"}) {
                List<String> expected = new ArrayList<>();
                for (ZipEntry entry : Collections.list(zipFile.entries())) {
                    if (!entry.isDirectory() && entry.getName().startsWith(prefix)) {
                        expected.add(entry.getName());
                    }
                }
                List<String> actual = new ArrayList<>();
                for (ZipEntry entry : Collections.list(entries(zipFile, prefix))) {
                    actual.add(entry.getName());
                }
                if (!expected.equals(actual)) {
                    throw new AssertionError(prefix + ": " + expected + " != " + actual);
                }
            }
        }
        Files.delete(file);
        System.out.println("ZipPackIndex ok");
    }
}
//?}
