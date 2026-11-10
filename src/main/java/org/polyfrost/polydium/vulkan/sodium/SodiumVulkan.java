package org.polyfrost.polydium.vulkan.sodium;

import com.mojang.blaze3d.textures.GpuTextureView;
import org.polyfrost.polydium.vulkan.ShineVulkan;
import org.polyfrost.polydium.vulkan.VulkanActive;
import org.polyfrost.polydium.vulkan.VulkanConst;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.buffers.Std140Builder;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gl.device.MultiDrawBatch;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.SharedQuadIndexBuffer;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.util.UInt32;
import net.caffeinemc.mods.sodium.mixin.core.render.texture.TextureAtlasAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.lwjgl.vulkan.VkMultiDrawIndexedInfoEXT;
import org.polyfrost.polydium.mixin.PolydiumMixinPlugin;
import org.polyfrost.polydium.mixin.client.perf.sodium.vulkan.MixinVulkanChunkRendererInvoker;
import org.polyfrost.polydium.vulkan.MoulberryVulkan;
import org.polyfrost.polydium.vulkan.VulkanDevice;
import org.polyfrost.polydium.vulkan.VulkanRenderPass;
import org.polyfrost.polydium.vulkan.VulkanRenderPipeline;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class SodiumVulkan {
    public static GpuBuffer sectionTimeInfo;

    private static final boolean SODIUM_08 = PolydiumMixinPlugin.SODIUM_V08;
    private static DynamicUniformStorage<Globals> globals08;
    private static final MethodHandle PREPARE_CHUNK_DATA;

    static {
        MethodHandle prepare = null;
        if (SODIUM_08) {
            try {
                prepare = MethodHandles.publicLookup().findVirtual(RenderRegion.DeviceResources.class, "prepareChunkData",
                        MethodType.methodType(GlBuffer.class, CommandList.class));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Sodium 0.8.x without RenderRegion.DeviceResources.prepareChunkData", e);
            }
        }
        PREPARE_CHUNK_DATA = prepare;
    }

    private static final VertexFormat VERTEX_FORMAT = VertexFormat.builder()
            .add("a_Position", VulkanConst.vertexElement(VertexFormatElement.Type.UINT, 2))
            .add("a_Color", VertexFormatElement.COLOR)
            .add("a_TexCoord", VulkanConst.vertexElement(VertexFormatElement.Type.USHORT, 2))
            .add("a_LightAndData", VulkanConst.vertexElement(VertexFormatElement.Type.UBYTE, 4))
            .build();


    private static final Identifier SHADER = Identifier.fromNamespaceAndPath("polydium", "sodium/block_layer_opaque");
    private static final Map<TerrainRenderPass, RenderPipeline> PIPELINES = new Reference2ReferenceOpenHashMap<>();

    private static final int INDIRECT_INITIAL_SIZE = 512_000;
    private static final int USAGE_INDIRECT_PARAMETERS = 512;
    private static MappableRingBuffer indirectRing;
    private static GpuBuffer.MappedView indirectView;
    private static int indirectOffset;
    private static int indirectSize;

    private static long multiDrawScratch;
    private static int multiDrawScratchCapacity;

    private SodiumVulkan() {
    }

    private static RenderPipeline pipeline(TerrainRenderPass pass) {
        return PIPELINES.computeIfAbsent(pass, p -> {
            RenderPipeline vanilla = p.getPipeline();
            RenderPipeline.Builder builder = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("sodium", vanilla.getLocation().getPath()))
                    .withVertexShader(SHADER)
                    .withFragmentShader(SHADER)
                    .withSampler("u_LightTex")
                    .withSampler("u_BlockTex")
                    .withUniform("u_Globals", UniformType.UNIFORM_BUFFER)
                    .withCull(vanilla.isCull())
                    .withVertexFormat(VERTEX_FORMAT, VertexFormat.Mode.QUADS)
                    .withShaderDefine("USE_VERTEX_COMPRESSION")
                    .withShaderDefine("USE_FOG");
            if (SODIUM_08) {
                builder.withUniform("ChunkData", UniformType.UNIFORM_BUFFER).withShaderDefine("SODIUM_REGION_FADE");
            } else {
                builder.withUniform("u_SectionTimeInfo", UniformType.TEXEL_BUFFER, TextureFormat.RED8I);
            }
            VulkanRenderPipeline.withColor(builder, VulkanRenderPipeline.colorState(vanilla));
            VulkanRenderPipeline.withDepth(builder, VulkanRenderPipeline.depthState(vanilla));
            if (p.isTranslucent()) {
                builder.withShaderDefine("ALPHA_CUTOUT", 0.01f);
            } else if (p.supportsFragmentDiscard()) {
                builder.withShaderDefine("ALPHA_CUTOUT", 0.5f);
            }
            if (!ShineVulkan.LOADED) {
                return builder.build();
            }
            RenderPipeline pipeline = builder.withSampler("u_ShineMaskTex").withUniform("ShineTerrain", UniformType.UNIFORM_BUFFER).build();
            ColorTargetState scene = VulkanRenderPipeline.colorState(vanilla);
            VulkanActive.COLOR_TARGETS.put(pipeline, new ColorTargetState[]{scene, scene, scene});
            return pipeline;
        });
    }

    public static void render(CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass,
                              CameraTransform camera, boolean indexedRenderingEnabled, SharedQuadIndexBuffer sharedIndexBuffer,
                              GpuSampler terrainSampler, GpuBufferSlice uniformBuffer) {
        final boolean useBlockFaceCulling = MoulberryVulkan.useBlockFaceCulling(SodiumClientMod.options().performance.useBlockFaceCulling);
        final boolean useIndexedTessellation = renderPass.isTranslucent() && indexedRenderingEnabled;

        Iterator<ChunkRenderList> iterator = renderLists.iterator(renderPass.isTranslucent());
        boolean hasDrawBatches = false;

        while (iterator.hasNext()) {
            ChunkRenderList renderList = iterator.next();

            var region = renderList.getRegion();
            var storage = region.getStorage(renderPass);

            if (storage == null) {
                continue;
            }

            if (region.getResources() == null) {
                region.clearCachedBatchFor(renderPass);
                continue;
            }

            var batch = region.getCachedBatch(renderPass);
            if (MoulberryVulkan.axiomChunksChanged()) batch.clear();
            if (!batch.isFilled) {
                MixinVulkanChunkRendererInvoker.polydium$fillCommandBuffer(batch, region, storage, renderList, camera, renderPass,
                        useBlockFaceCulling, useIndexedTessellation);
            }

            if (batch.isEmpty()) {
                continue;
            }

            hasDrawBatches = true;

            if (!useIndexedTessellation) {
                //? if >= 26.1 {
                sharedIndexBuffer.ensureCapacity(commandList, SODIUM_08 ? maxElementCount(batch) : UInt32.downcast(batch.maxElementCount));
                //?} else {
                /*sharedIndexBuffer.ensureCapacity(commandList, maxElementCount(batch));
                *///?}
            }
            if (SODIUM_08) {
                chunkData(region.getResources());
            }
        }

        if (hasDrawBatches) {
            draw(renderLists, renderPass, camera, useIndexedTessellation, sharedIndexBuffer, terrainSampler, uniformBuffer);
        }
    }

    private static int maxElementCount(MultiDrawBatch batch) {
        int elements = 0;
        for (int i = 0; i < batch.size; i++) {
            elements = Math.max(elements, MemoryUtil.memGetInt(batch.pElementCount + ((long) i << 2)));
        }
        return elements;
    }

    private static void draw(ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera,
                            boolean useIndexedTessellation, SharedQuadIndexBuffer sharedIndexBuffer,
                            GpuSampler terrainSampler, GpuBufferSlice uniformData) {
        RenderTarget target = renderPass.getTarget();
        VulkanDevice device = VulkanDevice.active;
        boolean multiDraw = device.getDeviceInfo().features().multiDrawDirectInterleaved();
        boolean indirect = !multiDraw && device.getDeviceInfo().features().multiDrawIndirect();

        if (indirect) {
            int commands = 0;
            Iterator<ChunkRenderList> counting = renderLists.iterator(renderPass.isTranslucent());
            while (counting.hasNext()) {
                RenderRegion region = counting.next().getRegion();
                if (region.getStorage(renderPass) != null && region.getResources() != null) {
                    commands += region.getCachedBatch(renderPass).size;
                }
            }
            reserveIndirect(commands * VkDrawIndexedIndirectCommand.SIZEOF);
        }

        if (ShineVulkan.LOADED) {
            ShineVulkan.beginPass();
            Iterator<ChunkRenderList> regions = renderLists.iterator(renderPass.isTranslucent());
            while (regions.hasNext()) {
                RenderRegion region = regions.next().getRegion();
                if (region.getStorage(renderPass) != null && region.getResources() != null && !region.getCachedBatch(renderPass).isEmpty()) {
                    ShineVulkan.prepareRegion(region);
                }
            }
        }

        VulkanRenderPass pass = ShineVulkan.LOADED ? ShineVulkan.createRenderPass(device, target)
                : (VulkanRenderPass) device.createCommandEncoder().createRenderPass(() -> "Terrain",
                target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty());
        try (MemoryStack stack = MemoryStack.stackPush()) {
            pass.setPipeline(pipeline(renderPass));

            if (!useIndexedTessellation) {
                pass.setIndexBuffer(VkCommandList.gpu(sharedIndexBuffer.getBufferObject()), VertexFormat.IndexType.INT);
            }

            pass.setUniform("u_Globals", uniformData);
            if (!SODIUM_08) {
                pass.setUniform("u_SectionTimeInfo", sectionTimeInfo);
            }
            //? if >= 26.1 {
            GpuTextureView lightmap = Minecraft.getInstance().gameRenderer.lightmap();
            //?} else {
            /*GpuTextureView lightmap = Minecraft.getInstance().gameRenderer.lightTexture().getTextureView();
            *///?}
            pass.bindTexture("u_LightTex", lightmap, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.bindTexture("u_BlockTex", renderPass.getAtlas(), terrainSampler);
            if (ShineVulkan.LOADED) {
                ShineVulkan.bindPass(pass, renderPass.getAtlas(), terrainSampler);
            }

            long pushConstants = stack.nmalloc(VulkanRenderPipeline.SODIUM_PUSH_CONSTANT_RANGE);
            Iterator<ChunkRenderList> iterator = renderLists.iterator(renderPass.isTranslucent());

            while (iterator.hasNext()) {
                RenderRegion region = iterator.next().getRegion();

                if (region.getStorage(renderPass) == null) {
                    continue;
                }

                RenderRegion.DeviceResources resources = region.getResources();

                if (resources == null) {
                    continue;
                }

                MultiDrawBatch batch = region.getCachedBatch(renderPass);

                if (batch.isEmpty()) {
                    continue;
                }

                if (useIndexedTessellation) {
                    pass.setIndexBuffer(VkCommandList.gpu(resources.getIndexBuffer()), VertexFormat.IndexType.INT);
                }

                pass.setVertexBuffer(0, VkCommandList.gpu(resources.getGeometryBuffer()));

                MemoryUtil.memPutFloat(pushConstants, cameraTranslation(region.getOriginX(), camera.intX, camera.fracX));
                MemoryUtil.memPutFloat(pushConstants + 4, cameraTranslation(region.getOriginY(), camera.intY, camera.fracY));
                MemoryUtil.memPutFloat(pushConstants + 8, cameraTranslation(region.getOriginZ(), camera.intZ, camera.fracZ));
                MemoryUtil.memPutInt(pushConstants + 12, Math.toIntExact(System.currentTimeMillis() - region.getCreationTime()));
                if (SODIUM_08) {
                    MemoryUtil.memPutInt(pushConstants + 16, 0);
                    pass.setUniform("ChunkData", VkCommandList.gpu(chunkData(resources)));
                } else {
                    //? if >= 26.1 {
                    MemoryUtil.memPutInt(pushConstants + 16, region.getId());
                    //?}
                }
                pass.pushConstants(pushConstants, VulkanRenderPipeline.SODIUM_PUSH_CONSTANT_RANGE);
                if (ShineVulkan.LOADED) {
                    ShineVulkan.bindRegion(pass);
                }

                if (indirect) {
                    drawBatchIndirect(pass, batch);
                } else {
                    drawBatch(pass, batch, multiDraw);
                }
            }
        } finally {
            pass.close();
            if (ShineVulkan.LOADED) {
                ShineVulkan.endPass();
            }
        }
    }

    public static GlBuffer chunkData(RenderRegion.DeviceResources resources) {
        try {
            return (GlBuffer) PREPARE_CHUNK_DATA.invoke(resources, (CommandList) VkCommandList.INSTANCE);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    public static GpuBufferSlice globals08(ChunkRenderMatrices matrices, FogParameters fog) {
        if (globals08 == null) {
            globals08 = new DynamicUniformStorage<>("Sodium terrain uniforms", 256, 8);
        }
        double subTexelPrecision = (1 << RenderDevice.INSTANCE.getSubTexelPrecisionBits());
        double subTexelOffset = 1.0f / CompactChunkVertex.TEXTURE_MAX_VALUE;
        var atlas = (TextureAtlasAccessor) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        return globals08.writeUniform(new Globals(
                new Matrix4f(matrices.projection()),
                new Matrix4f(matrices.modelView()),
                fog.red(), fog.green(), fog.blue(), fog.alpha(),
                fog.environmentalStart(), fog.environmentalEnd(),
                fog.renderStart(), fog.renderEnd(),
                1.0f / atlas.sodium$getWidth(), 1.0f / atlas.sodium$getHeight(),
                (float) (subTexelOffset - (((1.0D / atlas.sodium$getWidth()) / subTexelPrecision))),
                (float) (subTexelOffset - (((1.0D / atlas.sodium$getHeight()) / subTexelPrecision))),
                (float) (1.0 / (Minecraft.getInstance().options.chunkSectionFadeInTime().get() * 1000.0)),
                Minecraft.getInstance().options.textureFiltering().get() == TextureFilteringMethod.RGSS ? 1 : 0
        ));
    }

    private record Globals(Matrix4f projection, Matrix4f modelView, float fogRed, float fogGreen, float fogBlue, float fogAlpha,
                           float environmentalFogStart, float environmentalFogEnd, float renderFogStart, float renderFogEnd,
                           float textureAtlasWidthInverse, float textureAtlasHeightInverse, float subTexelOffsetX,
                           float subTexelOffsetY, float fadeInFactor, int useRgbaTextureFiltering)
            implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putMat4f(this.projection)
                    .putMat4f(this.modelView)
                    .putVec4(this.fogRed, this.fogGreen, this.fogBlue, this.fogAlpha)
                    .putVec2(this.environmentalFogStart, this.environmentalFogEnd)
                    .putVec2(this.renderFogStart, this.renderFogEnd)
                    .putVec2(this.textureAtlasWidthInverse, this.textureAtlasHeightInverse)
                    .putVec2(this.subTexelOffsetX, this.subTexelOffsetY)
                    .putFloat(this.fadeInFactor)
                    .putInt(this.useRgbaTextureFiltering)
                    .get();
        }
    }

    private static float cameraTranslation(int chunkBlockPos, int cameraBlockPos, float cameraPos) {
        return (chunkBlockPos - cameraBlockPos) - cameraPos;
    }

    private static void drawBatch(VulkanRenderPass pass, MultiDrawBatch batch, boolean multiDraw) {
        int size = batch.size;

        if (!multiDraw) {
            for (int i = 0; i < size; i++) {
                pass.drawIndexed(MemoryUtil.memGetInt(batch.pElementCount + ((long) i << 2)), 1, firstIndex(batch, i),
                        MemoryUtil.memGetInt(batch.pBaseVertex + ((long) i << 2)), 0);
            }
            return;
        }

        if (size > multiDrawScratchCapacity) {
            multiDrawScratch = MemoryUtil.nmemRealloc(multiDrawScratch, (long) size * VkMultiDrawIndexedInfoEXT.SIZEOF);
            multiDrawScratchCapacity = size;
        }

        for (int i = 0; i < size; i++) {
            long command = multiDrawScratch + (long) i * VkMultiDrawIndexedInfoEXT.SIZEOF;
            MemoryUtil.memPutInt(command + VkMultiDrawIndexedInfoEXT.FIRSTINDEX, firstIndex(batch, i));
            MemoryUtil.memPutInt(command + VkMultiDrawIndexedInfoEXT.INDEXCOUNT, MemoryUtil.memGetInt(batch.pElementCount + ((long) i << 2)));
            MemoryUtil.memPutInt(command + VkMultiDrawIndexedInfoEXT.VERTEXOFFSET, MemoryUtil.memGetInt(batch.pBaseVertex + ((long) i << 2)));
        }

        pass.multiDrawIndexed(MemoryUtil.memIntBuffer(multiDrawScratch, size * (VkMultiDrawIndexedInfoEXT.SIZEOF / 4)), 1, 0, size);
    }

    private static void drawBatchIndirect(VulkanRenderPass pass, MultiDrawBatch batch) {
        int size = batch.size;
        int bytes = size * VkDrawIndexedIndirectCommand.SIZEOF;
        long commands = MemoryUtil.memAddress(indirectView.data()) + indirectOffset;

        for (int i = 0; i < size; i++) {
            long command = commands + (long) i * VkDrawIndexedIndirectCommand.SIZEOF;
            MemoryUtil.memPutInt(command + VkDrawIndexedIndirectCommand.INDEXCOUNT, MemoryUtil.memGetInt(batch.pElementCount + ((long) i << 2)));
            MemoryUtil.memPutInt(command + VkDrawIndexedIndirectCommand.INSTANCECOUNT, 1);
            MemoryUtil.memPutInt(command + VkDrawIndexedIndirectCommand.FIRSTINDEX, firstIndex(batch, i));
            MemoryUtil.memPutInt(command + VkDrawIndexedIndirectCommand.VERTEXOFFSET, MemoryUtil.memGetInt(batch.pBaseVertex + ((long) i << 2)));
            MemoryUtil.memPutInt(command + VkDrawIndexedIndirectCommand.FIRSTINSTANCE, 0);
        }

        pass.drawIndexedIndirect(indirectRing.currentBuffer().slice(indirectOffset, bytes), size);
        indirectOffset += bytes;
    }

    private static void reserveIndirect(int bytes) {
        if (indirectRing != null && indirectOffset + bytes <= indirectSize) {
            return;
        }

        if (indirectRing != null) {
            indirectView.close();
            indirectRing.close();
        }

        indirectSize = Math.max(INDIRECT_INITIAL_SIZE, Math.max(bytes, indirectSize * 2));
        indirectRing = new MappableRingBuffer(() -> "Sodium indirect draws", GpuBuffer.USAGE_MAP_WRITE | USAGE_INDIRECT_PARAMETERS, indirectSize);
        mapIndirect();
    }

    private static void mapIndirect() {
        indirectView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(indirectRing.currentBuffer(), false, true);
        indirectOffset = 0;
    }

    public static void endFrame() {
        ShineVulkan.endFrame();
        if (globals08 != null) {
            globals08.endFrame();
        }
        if (indirectRing != null) {
            indirectView.close();
            indirectRing.rotate();
            mapIndirect();
        }
    }

    private static int firstIndex(MultiDrawBatch batch, int i) {
        return (int) (MemoryUtil.memGetAddress(batch.pElementPointer + ((long) i << Pointer.POINTER_SHIFT)) >> 2);
    }
}
