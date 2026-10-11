package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.blaze3d.systems.ScissorState;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;

import java.util.Objects;

public final class RenderTypeEquality {
    public static final Hash.Strategy<PreparedRenderType> STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(PreparedRenderType type) {
            return type == null ? 0 : hash(type);
        }

        @Override
        public boolean equals(PreparedRenderType a, PreparedRenderType b) {
            return a == b || a != null && b != null && same(a, b);
        }
    };

    private RenderTypeEquality() {
    }

    public static boolean same(PreparedRenderType a, PreparedRenderType b) {
        return a.pipeline() == b.pipeline()
            && sameScissor(a.scissorState(), b.scissorState())
            && Objects.equals(a.dynamicTransforms(), b.dynamicTransforms())
            //? if >= 26.3 {
            && Objects.equals(a.oitPipelineSet(), b.oitPipelineSet())
            && Objects.equals(a.name(), b.name())
            //?} else {
            /*&& Objects.equals(a.outputTarget(), b.outputTarget())
            *///?}
            && Objects.equals(a.textures(), b.textures());
    }

    public static int hash(PreparedRenderType type) {
        int hash = System.identityHashCode(type.pipeline());
        hash = 31 * hash + scissorHash(type.scissorState());
        hash = 31 * hash + Objects.hashCode(type.dynamicTransforms());
        return 31 * hash + Objects.hashCode(type.textures());
    }

    public static boolean sameScissor(ScissorState a, ScissorState b) {
        if (a == b)
            return true;
        if (a == null || b == null || a.enabled() != b.enabled())
            return false;
        return !a.enabled() || a.x() == b.x() && a.y() == b.y() && a.width() == b.width() && a.height() == b.height();
    }

    public static int scissorHash(ScissorState state) {
        if (state == null || !state.enabled())
            return 0;
        return 31 * (31 * (31 * (31 + state.x()) + state.y()) + state.width()) + state.height();
    }
}
//?}
