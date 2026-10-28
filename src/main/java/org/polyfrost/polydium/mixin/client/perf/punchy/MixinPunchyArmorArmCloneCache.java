package org.polyfrost.polydium.mixin.client.perf.punchy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
public class MixinPunchyArmorArmCloneCache {
    @Unique
    private static final Map<ModelPart, ModelPart> polydium$clones = new WeakHashMap<>();

    @WrapMethod(method = "cloneModelPart", remap = false, require = 0)
    private static ModelPart polydium$cachedClone(ModelPart source, Operation<ModelPart> original) {
        if (source == null || !RenderSystem.isOnRenderThread()) return original.call(source);
        ModelPart clone = polydium$clones.get(source);
        if (clone == null) {
            clone = original.call(source);
            if (clone != null) polydium$clones.put(source, clone);
            return clone;
        }
        clone.x = source.x;
        clone.y = source.y;
        clone.z = source.z;
        clone.xRot = source.xRot;
        clone.yRot = source.yRot;
        clone.zRot = source.zRot;
        clone.xScale = source.xScale;
        clone.yScale = source.yScale;
        clone.zScale = source.zScale;
        clone.visible = source.visible;
        clone.skipDraw = source.skipDraw;
        return clone;
    }
}
