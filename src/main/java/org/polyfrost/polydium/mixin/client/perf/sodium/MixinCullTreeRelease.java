package org.polyfrost.polydium.mixin.client.perf.sodium;

//? if >= 26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.async.CullTask;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.CullType;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import org.polyfrost.polydium.client.perf.SodiumCullTreePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Map;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinCullTreeRelease {
    @Shadow
    private SectionTree renderTree;

    @Shadow
    @Final
    private Map<CullType, SectionTree> cullResults;

    @Unique
    private final ArrayList<CullTask> polydium$consumedTasks = new ArrayList<>();

    @WrapOperation(
            method = "consumeCullTaskResults",
            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/async/CullTask;getResult()Ljava/lang/Object;")
    )
    private Object polydium$trackTask(CullTask task, Operation<Object> original) {
        Object result = original.call(task);
        this.polydium$consumedTasks.add(task);
        return result;
    }

    @Inject(method = "consumeCullTaskResults", at = @At("RETURN"))
    private void polydium$releaseDeadTrees(boolean waitForCompletion, CallbackInfo ci) {
        ArrayList<CullTask> tasks = this.polydium$consumedTasks;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            CullTask task = tasks.get(i);
            if (!this.polydium$frameInUse(task.getFrame())) {
                SodiumCullTreePool.release(((SodiumCullTreePool.Owner) task).polydium$treeArrays());
                tasks.remove(i);
            }
        }
    }

    @Unique
    private boolean polydium$frameInUse(int frame) {
        return polydium$isFrame(this.renderTree, frame)
                || polydium$isFrame(this.cullResults.get(CullType.LOCAL), frame)
                || polydium$isFrame(this.cullResults.get(CullType.REGULAR), frame)
                || polydium$isFrame(this.cullResults.get(CullType.WIDE), frame);
    }

    @Unique
    private static boolean polydium$isFrame(SectionTree tree, int frame) {
        return tree != null && tree.getFrame() == frame;
    }
}
//?}
