package org.polyfrost.polydium.client.perf;

//? if >= 26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.polyfrost.polydium.mixin.client.perf.BlockStateModelWrapperAccessor;

public final class FrameModels {
    private static final int VARIANTS = 4;

    private static final BlockModelRenderState[] STATES = new BlockModelRenderState[VARIANTS];
    private static final Submission[] SUBMISSIONS = new Submission[VARIANTS];
    private static final boolean[] PREPARED = new boolean[VARIANTS];

    private static BlockModelSet modelSet;

    private FrameModels() {
    }

    public static BlockModelRenderState prepared(BlockModelResolver resolver, boolean glowing, boolean map) {
        BlockModelSet current = Minecraft.getInstance().getModelManager().getBlockModelSet();

        if (current != modelSet) {
            modelSet = current;
            Arrays.fill(STATES, null);
            Arrays.fill(SUBMISSIONS, null);
            Arrays.fill(PREPARED, false);
        }

        int variant = (glowing ? 2 : 0) | (map ? 1 : 0);

        if (!PREPARED[variant]) {
            STATES[variant] = prepare(resolver, current, glowing, map);
            SUBMISSIONS[variant] = STATES[variant] == null ? null : Submission.capture(STATES[variant]);
            PREPARED[variant] = true;
        }

        return STATES[variant];
    }

    public static boolean isPrepared(BlockModelRenderState state) {
        for (BlockModelRenderState prepared : STATES) {
            if (prepared == state) {
                return true;
            }
        }
        return false;
    }

    public static Submission submission(BlockModelRenderState state) {
        for (int variant = 0; variant < VARIANTS; variant++) {
            if (STATES[variant] == state) {
                return SUBMISSIONS[variant];
            }
        }
        return null;
    }

    private static BlockModelRenderState prepare(BlockModelResolver resolver, BlockModelSet modelSet, boolean glowing, boolean map) {
        if (!isPlain(modelSet.get(BlockStateDefinitions.getItemFrameFakeState(glowing, map)))) {
            return null;
        }

        BlockModelRenderState state = new BlockModelRenderState();
        resolver.updateForItemFrame(state, glowing, map);
        return state;
    }

    private static boolean isPlain(BlockModel model) {
        if (model.getClass() != BlockStateModelWrapper.class) {
            return false;
        }

        BlockStateModelWrapperAccessor wrapper = (BlockStateModelWrapperAccessor) model;
        BlockStateModel inner = wrapper.polydium$model();
        if (!wrapper.polydium$tints().isEmpty() || inner.getClass() != SingleVariant.class) {
            return false;
        }

        List<BlockStateModelPart> parts = new ArrayList<>(1);
        inner.collectParts(RandomSource.create(42L), parts);
        return parts.size() == 1 && parts.get(0) instanceof SimpleModelWrapper part && part.quads().getClass() == QuadCollection.class;
    }

    private static int lightFor(BlockModelRenderState state, int lightCoords) {
        //? if >= 26.2 {
        return LightCoordsUtil.max(lightCoords, state.blockLightCoords);
        //?} else
        /*return lightCoords;*/
    }

    public record Submission(Function<ChunkSectionLayer, RenderType> renderTypes, boolean translucent, List<BlockStateModelPart> parts, Mesh mesh, int[] tints, BlockModelRenderState state) {
        private static final int SUBMIT_ARGUMENTS = 9;
        private static final int FIRST_LIGHT = LightCoordsUtil.pack(3, 11);
        private static final int SECOND_LIGHT = LightCoordsUtil.pack(14, 2);
        private static final int FIRST_OVERLAY = 0x000A0003;
        private static final int SECOND_OVERLAY = 0x00050007;
        private static final int FIRST_OUTLINE = 0xFF123456;
        private static final int SECOND_OUTLINE = 0;

        public boolean submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, int outlineColor) {
            if (this.renderTypes.apply(ChunkSectionLayer.SOLID) != RenderTypes.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)) {
                return false;
            }
            collector.submitBlockModel(poseStack, this.renderTypes, this.translucent, this.parts, this.mesh, this.tints, lightFor(this.state, lightCoords), overlayCoords, outlineColor);
            return true;
        }

        static Submission capture(BlockModelRenderState state) {
            Call first = record(state, FIRST_LIGHT, FIRST_OVERLAY, FIRST_OUTLINE);
            Call second = record(state, SECOND_LIGHT, SECOND_OVERLAY, SECOND_OUTLINE);

            if (first == null || second == null || !first.sameAs(second)) {
                return null;
            }

            if (!first.passes(state, FIRST_LIGHT, FIRST_OVERLAY, FIRST_OUTLINE) || !second.passes(state, SECOND_LIGHT, SECOND_OVERLAY, SECOND_OUTLINE)) {
                return null;
            }

            return new Submission(first.renderTypes, first.translucent, first.parts, first.mesh, first.tints, state);
        }

        private static Call record(BlockModelRenderState state, int lightCoords, int overlayCoords, int outlineColor) {
            PoseStack poseStack = new PoseStack();
            Recorder recorder = new Recorder(poseStack);

            state.submitWithZOffset(poseStack, (SubmitNodeCollector) Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(), new Class<?>[]{SubmitNodeCollector.class}, recorder), lightCoords, overlayCoords, outlineColor);

            return recorder.single();
        }
    }

    private record Call(Function<ChunkSectionLayer, RenderType> renderTypes, boolean translucent, List<BlockStateModelPart> parts, Mesh mesh, int[] tints, int lightCoords, int overlayCoords, int outlineColor) {
        private boolean sameAs(Call other) {
            if (this.translucent != other.translucent || !this.parts.equals(other.parts) || !Arrays.equals(this.tints, other.tints)) {
                return false;
            }

            for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                if (this.renderTypes.apply(layer) != other.renderTypes.apply(layer)) {
                    return false;
                }
            }

            return true;
        }

        private boolean passes(BlockModelRenderState state, int lightCoords, int overlayCoords, int outlineColor) {
            return this.lightCoords == lightFor(state, lightCoords) && this.overlayCoords == overlayCoords && this.outlineColor == outlineColor;
        }
    }

    private static final class Recorder implements InvocationHandler {
        private final PoseStack poseStack;
        private final Matrix4f pose;
        private Call call;
        private int calls;

        private Recorder(PoseStack poseStack) {
            this.poseStack = poseStack;
            this.pose = new Matrix4f(poseStack.last().pose());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args) {
            this.calls++;

            if (!method.getName().equals("submitBlockModel") || args == null || args.length != Submission.SUBMIT_ARGUMENTS || args[0] != this.poseStack || !this.poseStack.last().pose().equals(this.pose)) {
                return null;
            }

            if (args[1] == null || args[4] == null) {
                return null;
            }

            this.call = new Call((Function<ChunkSectionLayer, RenderType>) args[1], (Boolean) args[2], (List<BlockStateModelPart>) args[3], (Mesh) args[4], (int[]) args[5], (Integer) args[6], (Integer) args[7], (Integer) args[8]);

            return null;
        }

        private Call single() {
            return this.calls == 1 ? this.call : null;
        }
    }
}
//?}
