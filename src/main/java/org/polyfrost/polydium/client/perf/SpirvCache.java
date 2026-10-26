package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.lwjgl.Version;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.CRC32;

public final class SpirvCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FILE_MAGIC = 0x50505356;
    private static final int HEADER_SIZE = 8;
    private static final int SPIRV_MAGIC = 0x07230203;
    private static final int MIN_SPIRV_SIZE = 20;

    private static boolean warnedWrite;

    private static final class Game {
        static final Path DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("polydium").resolve("spirv");
        static final String VERSION = FabricLoader.getInstance().getModContainer("minecraft")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    private SpirvCache() {
    }

    //? if >= 26.3 {
    public static Path file(boolean zeroToOne, boolean drawParameters, String name, String source, ShaderType type, ShaderDefines defines, ShaderManager.Configs configs) {
    //?} else {
    /*public static Path file(String name, String source, ShaderType type) {
    *///?}
        try {
            MessageDigest digest = sha256();
            update(digest, "polydium-spirv-1");
            update(digest, Game.VERSION);
            update(digest, Version.getVersion());
            //? if >= 26.3 {
            update(digest, Boolean.toString(zeroToOne));
            update(digest, Boolean.toString(drawParameters));
            update(digest, Boolean.toString(RenderSystem.getDevice().getDeviceInfo().hintsAndWorkarounds().isExplicitDepthRequired()));
            //?}
            update(digest, name);
            update(digest, type.name());
            update(digest, source);
            //? if >= 26.3 {
            for (Map.Entry<String, String> macro : new TreeMap<>(defines.values()).entrySet()) {
                update(digest, macro.getKey());
                update(digest, macro.getValue());
            }
            update(digest, "flags");
            for (String flag : new TreeSet<>(defines.flags())) {
                update(digest, flag);
            }
            digest.update(includesDigest(configs));
            //?}
            return Game.DIRECTORY.resolve(HexFormat.of().formatHex(digest.digest(), 0, 20) + ".spv");
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static ByteBuffer read(Path file) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file);
        } catch (IOException | RuntimeException e) {
            return null;
        }
        int size = bytes.length - HEADER_SIZE;
        if (size < MIN_SPIRV_SIZE || size % 4 != 0) {
            return null;
        }
        ByteBuffer header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        CRC32 crc = new CRC32();
        crc.update(bytes, HEADER_SIZE, size);
        if (header.getInt(0) != FILE_MAGIC || header.getInt(4) != (int) crc.getValue() || header.getInt(HEADER_SIZE) != SPIRV_MAGIC) {
            return null;
        }
        ByteBuffer spirv = MemoryUtil.memAlloc(size);
        spirv.put(0, bytes, HEADER_SIZE, size);
        return spirv;
    }

    public static void write(Path file, ByteBuffer spirv) {
        try {
            byte[] bytes = new byte[HEADER_SIZE + spirv.remaining()];
            spirv.get(spirv.position(), bytes, HEADER_SIZE, spirv.remaining());
            CRC32 crc = new CRC32();
            crc.update(bytes, HEADER_SIZE, bytes.length - HEADER_SIZE);
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(0, FILE_MAGIC).putInt(4, (int) crc.getValue());
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "spv", ".tmp");
            try {
                Files.write(temporary, bytes);
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException | RuntimeException e) {
            if (!warnedWrite) {
                warnedWrite = true;
                LOGGER.warn("Could not write shader cache file {}", file, e);
            }
        }
    }

    //? if >= 26.3 {
    private static byte[] includesDigest(ShaderManager.Configs configs) {
        ShaderIncludesDigest holder = (ShaderIncludesDigest) (Object) configs;
        byte[] cached = holder.polydium$includesDigest();
        if (cached != null) {
            return cached;
        }
        List<Identifier> ids = new ArrayList<>(configs.includeSources().keySet());
        ids.sort(null);
        MessageDigest digest = sha256();
        for (Identifier id : ids) {
            update(digest, id.toString());
            ShaderSource.CachedIncludeSource include = configs.getInclude(id);
            // Minecraft doesn't null-terminate include contents, so content() would read past the allocation
            ByteBuffer contents = include == null ? null : MemoryUtil.memByteBufferSafe(
                    MemoryUtil.memGetAddress(include.includeResultPtr() + ShadercIncludeResult.CONTENT),
                    (int) ShadercIncludeResult.ncontent_length(include.includeResultPtr()));
            digest.update(longBytes(contents == null ? -1 : contents.remaining()));
            if (contents != null) {
                digest.update(contents);
            }
        }
        byte[] result = digest.digest();
        holder.polydium$setIncludesDigest(result);
        return result;
    }
    //?}

    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(longBytes(bytes.length));
        digest.update(bytes);
    }

    private static byte[] longBytes(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("polydium-spirv", ".spv");
        ByteBuffer spirv = MemoryUtil.memAlloc(24).order(ByteOrder.nativeOrder());
        spirv.putInt(0, SPIRV_MAGIC).putInt(20, 42);
        write(file, spirv);
        ByteBuffer back = read(file);
        check(back != null && back.remaining() == 24 && back.order(ByteOrder.nativeOrder()).getInt(20) == 42, "round trip");
        byte[] bytes = Files.readAllBytes(file);
        bytes[bytes.length - 1] ^= 1;
        Files.write(file, bytes);
        check(read(file) == null, "corrupt payload");
        Files.write(file, Arrays.copyOf(bytes, 12));
        check(read(file) == null, "truncated");
        Files.delete(file);
        check(read(file) == null, "missing");
        System.out.println("SpirvCache ok");
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }
}
//?}
