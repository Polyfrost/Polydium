package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.1 && < 26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(DecoratedPotRenderer.class)
public abstract class MixinDecoratedPotSpriteCache {

    @Shadow
    @Final
    private SpriteGetter sprites;

    @Unique
    private final Map<SpriteId, Pair<RenderType, TextureAtlasSprite>> polydium$sides = new HashMap<>();

    @Unique
    private SpriteId polydium$lastId;

    @Unique
    private Pair<RenderType, TextureAtlasSprite> polydium$lastSide;

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/world/level/block/entity/PotDecorations;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/sprite/SpriteId;renderType(Ljava/util/function/Function;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType polydium$cachedRenderType(SpriteId id, Function<?, RenderType> factory, Operation<RenderType> original) {
        Pair<RenderType, TextureAtlasSprite> side = this.polydium$sides.get(id);
        if (side == null) {
            side = Pair.of(original.call(id, factory), this.sprites.get(id));
            this.polydium$sides.put(id, side);
        }
        this.polydium$lastId = id;
        this.polydium$lastSide = side;
        return side.getFirst();
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/world/level/block/entity/PotDecorations;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/sprite/SpriteGetter;get(Lnet/minecraft/client/resources/model/sprite/SpriteId;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite polydium$cachedSprite(SpriteGetter sprites, SpriteId id, Operation<TextureAtlasSprite> original) {
        return id == this.polydium$lastId ? this.polydium$lastSide.getSecond() : original.call(sprites, id);
    }
}
*///?} else if >= 1.21.10 && < 26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DecoratedPotRenderer.class)
public abstract class MixinDecoratedPotSpriteCache {

    @Unique
    private final Reference2ObjectOpenHashMap<Material, TextureAtlasSprite> polydium$sprites = new Reference2ObjectOpenHashMap<>();

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/world/level/block/entity/PotDecorations;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/MaterialSet;get(Lnet/minecraft/client/resources/model/Material;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite polydium$cachedSprite(MaterialSet materials, Material material, Operation<TextureAtlasSprite> original) {
        TextureAtlasSprite sprite = this.polydium$sprites.get(material);
        if (sprite == null) {
            sprite = original.call(materials, material);
            this.polydium$sprites.put(material, sprite);
        }
        return sprite;
    }
}
*///?} else if < 1.21.10 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import org.polyfrost.polydium.client.perf.SpriteHandoff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(DecoratedPotRenderer.class)
public abstract class MixinDecoratedPotSpriteCache {

    @Unique
    private final Reference2ObjectOpenHashMap<Material, TextureAtlasSprite> polydium$sprites = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = {/^? if >= 1.21.4 {^/"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/level/block/entity/PotDecorations;)V"/^?} else {^//^"render(Lnet/minecraft/world/level/block/entity/DecoratedPotBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"^//^?}^/, "renderSide"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;buffer(Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer polydium$cachedBuffer(Material material, MultiBufferSource buffers, Function factory, Operation<VertexConsumer> original) {
        if (!((Object) material instanceof SpriteHandoff handoff)) {
            return original.call(material, buffers, factory);
        }
        TextureAtlasSprite sprite = this.polydium$sprites.get(material);
        if (sprite == null) {
            sprite = material.sprite();
            this.polydium$sprites.put(material, sprite);
        }
        handoff.polydium$handSprite(sprite);
        try {
            return original.call(material, buffers, factory);
        } finally {
            handoff.polydium$handSprite(null);
        }
    }
}
*///?}
