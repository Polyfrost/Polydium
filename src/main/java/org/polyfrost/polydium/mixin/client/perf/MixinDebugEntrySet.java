package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugScreenEntryList.class)
public class MixinDebugEntrySet {
    @Shadow
    private long currentlyEnabledVersion;

    @Unique
    private Set<Object> polydium$enabledSet;
    @Unique
    private long polydium$enabledSetVersion;

    //? if >= 26.2 {
    @Unique
    private List<Object> polydium$enabledCopy;
    @Unique
    private long polydium$enabledCopyVersion;

    @WrapOperation(
        method = "getCurrentlyEnabled",
        at = @At(value = "INVOKE", target = "Ljava/util/List;copyOf(Ljava/util/Collection;)Ljava/util/List;")
    )
    private List<Object> polydium$reuseEnabledCopy(Collection<Object> currentlyEnabled, Operation<List<Object>> original) {
        List<Object> copy = this.polydium$enabledCopy;
        if (copy == null || this.polydium$enabledCopyVersion != this.currentlyEnabledVersion) {
            copy = original.call(currentlyEnabled);
            this.polydium$enabledCopy = copy;
            this.polydium$enabledCopyVersion = this.currentlyEnabledVersion;
        }
        return copy;
    }
    //?}

    @WrapOperation(
        method = "isCurrentlyEnabled",
        at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z")
    )
    private boolean polydium$lookupInSet(List<Object> currentlyEnabled, Object location, Operation<Boolean> original) {
        Set<Object> set = this.polydium$enabledSet;
        if (set == null || this.polydium$enabledSetVersion != this.currentlyEnabledVersion) {
            set = new HashSet<>(currentlyEnabled);
            this.polydium$enabledSet = set;
            this.polydium$enabledSetVersion = this.currentlyEnabledVersion;
        }
        return set.contains(location);
    }
}
//?}
