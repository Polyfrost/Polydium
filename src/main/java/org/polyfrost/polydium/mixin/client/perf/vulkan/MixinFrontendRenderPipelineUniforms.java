package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 26.3 {
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.polyfrost.polydium.client.perf.PipelineUniforms;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FrontendRenderPipeline.class)
public abstract class MixinFrontendRenderPipelineUniforms implements PipelineUniforms {
    @Shadow
    @Final
    private Object2IntMap<String> uniformIndices;

    @Unique
    private String[] polydium$names;

    @Unique
    private int[] polydium$slots;

    @Unique
    private long polydium$validatedPass;

    @Override
    public long polydium$validatedPass() {
        return this.polydium$validatedPass;
    }

    @Override
    public void polydium$validatedPass(long pass) {
        this.polydium$validatedPass = pass;
    }

    @Override
    public String[] polydium$uniformNames() {
        if (this.polydium$names == null) {
            this.polydium$flatten();
        }
        return this.polydium$names;
    }

    @Override
    public int[] polydium$uniformSlots() {
        if (this.polydium$names == null) {
            this.polydium$flatten();
        }
        return this.polydium$slots;
    }

    @Unique
    private void polydium$flatten() {
        String[] names = new String[this.uniformIndices.size()];
        int[] slots = new int[names.length];
        int index = 0;
        for (Object2IntMap.Entry<String> uniform : this.uniformIndices.object2IntEntrySet()) {
            names[index] = uniform.getKey();
            slots[index] = uniform.getIntValue();
            index++;
        }
        this.polydium$slots = slots;
        this.polydium$names = names;
    }
}
//?}
