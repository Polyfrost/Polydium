package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.renderer.texture.SpriteContents;

public final class AnimatedSpriteOrder {
    private AnimatedSpriteOrder() {
    }

    public interface Interpolated {
        boolean polydium$interpolateFrames();

        void polydium$interpolateFrames(boolean interpolate);
    }

    public static ImmutableList<SpriteContents.AnimationState> group(List<SpriteContents.AnimationState> states) {
        ImmutableList.Builder<SpriteContents.AnimationState> grouped = ImmutableList.builderWithExpectedSize(states.size());

        for (SpriteContents.AnimationState state : states) {
            if (((Interpolated) state).polydium$interpolateFrames())
                grouped.add(state);
        }

        for (SpriteContents.AnimationState state : states) {
            if (!((Interpolated) state).polydium$interpolateFrames())
                grouped.add(state);
        }

        return grouped.build();
    }
}
//?}
