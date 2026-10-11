package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.zip.CRC32;

public interface VulkanPipelineCache {
    int HEADER_SIZE = 32;
    int CRC_SIZE = 4;

    long polydium$pipelineCache();

    Lock polydium$pipelineCacheUse();

    static boolean valid(byte[] blob) {
        if (blob.length <= HEADER_SIZE) {
            return false;
        }
        ByteBuffer header = ByteBuffer.wrap(blob).order(ByteOrder.nativeOrder());
        return header.getInt(0) == HEADER_SIZE && header.getInt(4) == 1;
    }

    static byte[] load(Path file) {
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (bytes.length <= CRC_SIZE || ByteBuffer.wrap(bytes).getInt(0) != crc(bytes)) {
                return null;
            }
            byte[] blob = Arrays.copyOfRange(bytes, CRC_SIZE, bytes.length);
            return valid(blob) ? blob : null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    static void save(Path file, byte[] blob) {
        if (!valid(blob)) {
            return;
        }
        try {
            byte[] bytes = new byte[CRC_SIZE + blob.length];
            System.arraycopy(blob, 0, bytes, CRC_SIZE, blob.length);
            ByteBuffer.wrap(bytes).putInt(0, crc(bytes));
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "pipelines", ".tmp");
            try {
                Files.write(temporary, bytes);
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException | RuntimeException e) {
            LogUtils.getLogger().warn("Could not write pipeline cache file {}", file, e);
        }
    }

    private static int crc(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes, CRC_SIZE, bytes.length - CRC_SIZE);
        return (int) crc.getValue();
    }
}
//?}
