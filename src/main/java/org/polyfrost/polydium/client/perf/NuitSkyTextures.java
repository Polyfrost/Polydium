package org.polyfrost.polydium.client.perf;

//? if nuit {
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.polyfrost.polydium.mixin.client.perf.TextureManagerAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if >= 26.2 {
import com.mojang.renderpearl.api.GpuFormat;
//?} else
/*import com.mojang.blaze3d.textures.TextureFormat;*/

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
//?}

public final class NuitSkyTextures {
    //? if nuit {
    private static final Logger LOGGER = LoggerFactory.getLogger("Polydium");
    private static final long EVICT_TICKS = 600;
    private static final long UPLOAD_BYTES_PER_TICK = 8L << 20;

    private static final Set<Identifier> MANAGED = new HashSet<>();
    private static final Object2LongOpenHashMap<Identifier> LAST_USED = new Object2LongOpenHashMap<>();
    private static final Map<Identifier, StreamedTexture> PENDING = new HashMap<>();
    private static final Queue<Decoded> DECODED = new ConcurrentLinkedQueue<>();
    private static final ArrayDeque<StreamedTexture> UPLOADING = new ArrayDeque<>();
    private static long ticks;

    private record Decoded(StreamedTexture texture, TextureContents contents) {
    }

    private NuitSkyTextures() {
    }

    public static void manage(Identifier id) {
        MANAGED.add(id);
    }

    public static boolean isReady(Identifier id) {
        return !MANAGED.contains(id) || byPath().containsKey(id);
    }

    public static void use(Identifier id) {
        LAST_USED.put(id, ticks);
        if (!PENDING.containsKey(id) && !byPath().containsKey(id)) {
            load(id);
        }
    }

    public static void tick() {
        long now = ++ticks;
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        Decoded decoded;
        while ((decoded = DECODED.poll()) != null) {
            if (PENDING.get(decoded.texture.id) == decoded.texture) {
                decoded.texture.begin(decoded.contents);
                UPLOADING.add(decoded.texture);
            } else {
                decoded.contents.close();
            }
        }

        long budget = UPLOAD_BYTES_PER_TICK;
        StreamedTexture uploading;
        while (budget > 0 && (uploading = UPLOADING.peek()) != null) {
            if (PENDING.get(uploading.id) != uploading) {
                UPLOADING.poll();
                continue;
            }
            budget -= uploading.upload(budget);
            if (uploading.isComplete()) {
                UPLOADING.poll();
                PENDING.remove(uploading.id);
                textures.register(uploading.id, uploading);
            }
        }

        if (now % 20 == 0) {
            LAST_USED.object2LongEntrySet().removeIf(entry -> {
                if (now - entry.getLongValue() < EVICT_TICKS) {
                    return false;
                }
                drop(entry.getKey());
                textures.release(entry.getKey());
                return true;
            });
        }
    }

    public static void clear() {
        PENDING.values().forEach(StreamedTexture::close);
        PENDING.clear();
        UPLOADING.clear();
    }

    private static void drop(Identifier id) {
        StreamedTexture texture = PENDING.remove(id);
        if (texture != null) {
            texture.close();
        }
    }

    private static void load(Identifier id) {
        StreamedTexture texture = new StreamedTexture(id);
        PENDING.put(id, texture);
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        CompletableFuture.supplyAsync(() -> decode(texture, resources), Util.backgroundExecutor())
                .thenAccept(contents -> DECODED.add(new Decoded(texture, contents)));
    }

    private static TextureContents decode(StreamedTexture texture, ResourceManager resources) {
        try {
            return texture.loadContents(resources);
        } catch (FileNotFoundException e) {
            return TextureContents.createMissing();
        } catch (Exception e) {
            LOGGER.error("Failed to load Nuit sky texture {}", texture.id, e);
            return TextureContents.createMissing();
        }
    }

    private static Map<Identifier, ?> byPath() {
        return ((TextureManagerAccessor) Minecraft.getInstance().getTextureManager()).polydium$byPath();
    }

    private static final class StreamedTexture extends SimpleTexture {
        private final Identifier id;
        private @Nullable NativeImage image;
        private int uploadedRows;

        private StreamedTexture(Identifier id) {
            super(id);
            this.id = id;
        }

        private void begin(TextureContents contents) {
            AddressMode address = contents.clamp() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
            FilterMode filter = contents.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
            this.sampler = RenderSystem.getSamplerCache().getSampler(address, address, filter, filter, false);

            NativeImage image = contents.image();
            GpuDevice device = RenderSystem.getDevice();
            this.releaseTextures();
            //? if >= 26.2 {
            this.texture = device.createTexture(this.id::toString, 5, GpuFormat.RGBA8_UNORM, image.getWidth(), image.getHeight(), 1, 1);
            //?} else
            /*this.texture = device.createTexture(this.id::toString, 5, TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1, 1);*/
            this.textureView = device.createTextureView(this.texture);
            this.image = image;
            this.uploadedRows = 0;
        }

        private long upload(long budget) {
            NativeImage image = this.image;
            if (image == null) {
                return 0;
            }
            int width = image.getWidth();
            int height = image.getHeight();
            long rowBytes = (long) width * 4;
            int rows = (int) Math.min(height - this.uploadedRows, Math.max(1, budget / rowBytes));
            int y = this.uploadedRows;
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            //? if >= 26.2 {
            encoder.writeToTexture(this.texture, image.getPixelBytes().slice((int) (y * rowBytes), (int) (rows * rowBytes)), 0, 0, 0, y, width, rows);
            //?} else
            /*encoder.writeToTexture(this.texture, image, 0, 0, 0, y, width, rows, 0, y);*/
            this.uploadedRows += rows;
            if (this.uploadedRows >= height) {
                image.close();
                this.image = null;
            }
            return rows * rowBytes;
        }

        private boolean isComplete() {
            return this.image == null && this.texture != null;
        }

        @Override
        public void close() {
            super.close();
            if (this.image != null) {
                this.image.close();
                this.image = null;
            }
        }
    }
    //?}
}
