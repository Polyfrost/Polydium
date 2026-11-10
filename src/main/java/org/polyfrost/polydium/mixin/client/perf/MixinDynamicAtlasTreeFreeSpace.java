package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import net.minecraft.client.renderer.texture.DynamicAtlasTree;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DynamicAtlasTree.class, priority = 999)
public abstract class MixinDynamicAtlasTreeFreeSpace {
    @Unique
    private static final int NIBBLE = 4;
    @Unique
    private static final int NIBBLE_MASK = (1 << NIBBLE) - 1;

    @Shadow
    @Final
    private int x;
    @Shadow
    @Final
    private int y;
    @Shadow
    @Final
    private int width;
    @Shadow
    @Final
    private int height;
    @Shadow
    private @Nullable DynamicAtlasTree left;
    @Shadow
    private @Nullable DynamicAtlasTree right;
    @Shadow
    private boolean occupied;

    @Unique
    private byte polydium$freeBits;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$startFree(int x, int y, int width, int height, CallbackInfo ci) {
        this.polydium$refresh();
    }

    @Overwrite
    private @Nullable DynamicAtlasTree insertInner(int slotWidth, int slotHeight, int spacing) {
        int free = this.polydium$freeBits;

        if (!polydium$fits(slotWidth, free & NIBBLE_MASK) || !polydium$fits(slotHeight, free >>> NIBBLE & NIBBLE_MASK))
            return null;

        DynamicAtlasTree inserted;

        if (this.left != null && this.right != null) {
            inserted = polydium$of(this.left).insertInner(slotWidth, slotHeight, spacing);

            if (inserted == null)
                inserted = polydium$of(this.right).insertInner(slotWidth, slotHeight, spacing);
        } else {
            if (this.occupied || slotWidth > this.width || slotHeight > this.height)
                return null;

            if (slotWidth == this.width && slotHeight == this.height) {
                this.occupied = true;
                this.polydium$refresh();

                return (DynamicAtlasTree) (Object) this;
            }

            if (this.width - slotWidth > this.height - slotHeight) {
                this.left  = new DynamicAtlasTree(this.x, this.y, slotWidth, this.height);
                this.right = new DynamicAtlasTree(this.x + slotWidth + spacing, this.y, this.width - slotWidth - spacing, this.height);
            } else {
                this.left  = new DynamicAtlasTree(this.x, this.y, this.width, slotHeight);
                this.right = new DynamicAtlasTree(this.x, this.y + slotHeight + spacing, this.width, this.height - slotHeight - spacing);
            }

            inserted = polydium$of(this.left).insertInner(slotWidth, slotHeight, spacing);
        }

        this.polydium$refresh();

        return inserted;
    }

    @Unique
    private void polydium$refresh() {
        if (this.left != null && this.right != null) {
            int left  = polydium$of(this.left).polydium$freeBits;
            int right = polydium$of(this.right).polydium$freeBits;

            this.polydium$freeBits = (byte) (Math.max(left & NIBBLE_MASK, right & NIBBLE_MASK) | Math.max(left >>> NIBBLE & NIBBLE_MASK, right >>> NIBBLE & NIBBLE_MASK) << NIBBLE);

            return;
        }

        this.polydium$freeBits = this.occupied ? 0 : (byte) (polydium$bits(this.width) | polydium$bits(this.height) << NIBBLE);
    }

    @Unique
    private static boolean polydium$fits(int size, int bits) {
        return bits == NIBBLE_MASK || size < 1 << bits;
    }

    @Unique
    private static int polydium$bits(int size) {
        return Math.min(Integer.SIZE - Integer.numberOfLeadingZeros(Math.max(size, 0)), NIBBLE_MASK);
    }

    @Unique
    private static MixinDynamicAtlasTreeFreeSpace polydium$of(DynamicAtlasTree tree) {
        return (MixinDynamicAtlasTreeFreeSpace) (Object) tree;
    }
}
//?}
