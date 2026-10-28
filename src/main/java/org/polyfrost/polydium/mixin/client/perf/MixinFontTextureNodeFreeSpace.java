package org.polyfrost.polydium.mixin.client.perf;

//? if < 26.3 {
/*//? if >= 1.21.10 {
import com.mojang.blaze3d.font.GlyphBitmap;
//?} else {
/^import com.mojang.blaze3d.font.SheetGlyphInfo;
^///?}
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.font.FontTexture$Node", priority = 999)
public abstract class MixinFontTextureNodeFreeSpace {
    @Unique
    private static final int NIBBLE = 4;
    @Unique
    private static final int NIBBLE_MASK = (1 << NIBBLE) - 1;

    @Shadow
    @Final
    private int width;
    @Shadow
    @Final
    private int height;
    @Shadow
    private boolean occupied;

    @Unique
    private byte polydium$freeBits;
    @Unique
    private MixinFontTextureNodeFreeSpace polydium$left;
    @Unique
    private MixinFontTextureNodeFreeSpace polydium$right;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void polydium$startFree(int x, int y, int width, int height, CallbackInfo ci) {
        this.polydium$refresh();
    }

    //? if >= 1.21.10 {
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void polydium$skipFull(GlyphBitmap glyph, CallbackInfoReturnable<Object> cir) {
    //?} else {
    /^@Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void polydium$skipFull(SheetGlyphInfo glyph, CallbackInfoReturnable<Object> cir) {
    ^///?}
        int free = this.polydium$freeBits;

        if (!polydium$fits(glyph.getPixelWidth(), free & NIBBLE_MASK) || !polydium$fits(glyph.getPixelHeight(), free >>> NIBBLE & NIBBLE_MASK))
            cir.setReturnValue(null);
    }

    //? if >= 1.21.10 {
    @Inject(method = "insert", at = @At("RETURN"))
    private void polydium$refreshAfterInsert(GlyphBitmap glyph, CallbackInfoReturnable<Object> cir) {
    //?} else {
    /^@Inject(method = "insert", at = @At("RETURN"))
    private void polydium$refreshAfterInsert(SheetGlyphInfo glyph, CallbackInfoReturnable<Object> cir) {
    ^///?}
        this.polydium$refresh();
    }

    @WrapOperation(method = "insert", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/font/FontTexture$Node;left:Lnet/minecraft/client/gui/font/FontTexture$Node;", opcode = Opcodes.PUTFIELD))
    private void polydium$trackLeft(MixinFontTextureNodeFreeSpace node, MixinFontTextureNodeFreeSpace child, Operation<Void> original) {
        original.call(node, child);
        node.polydium$left = child;
    }

    @WrapOperation(method = "insert", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/font/FontTexture$Node;right:Lnet/minecraft/client/gui/font/FontTexture$Node;", opcode = Opcodes.PUTFIELD))
    private void polydium$trackRight(MixinFontTextureNodeFreeSpace node, MixinFontTextureNodeFreeSpace child, Operation<Void> original) {
        original.call(node, child);
        node.polydium$right = child;
    }

    @Unique
    private void polydium$refresh() {
        if (this.polydium$left != null && this.polydium$right != null) {
            int left  = this.polydium$left.polydium$freeBits;
            int right = this.polydium$right.polydium$freeBits;

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
}
*///?}
