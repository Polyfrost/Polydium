package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
//? if >= 26.2 {
import net.minecraft.client.renderer.feature.GizmoFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}
import net.minecraft.client.renderer.rendertype.RenderTypes;
//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.CameraRenderState;
*///?}
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.polyfrost.polydium.client.perf.DebugLineClip;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

//? if >= 26.2 {
@Mixin(GizmoFeatureRenderer.class)
public abstract class MixinDebugLineFastPath extends RenderTypeFeatureRenderer<GizmoFeatureRenderer.Submit> {
//?} else {
/*@Mixin(targets = "net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives$Group")
public abstract class MixinDebugLineFastPath {
*///?}
    @Unique
    private static final Matrix4f POLYDIUM_IDENTITY = new Matrix4f();

    @Unique
    private final float[] polydium$line = new float[6];

    @Shadow
    @Final
    //? if >= 26.2 {
    private PoseStack poseStack;
    //?} else {
    /*private boolean opaque;

    @Shadow
    @Final
    private List<?> lines;
    *///?}

    //? if >= 26.2 {
    @Inject(method = "buildLines", at = @At("HEAD"), cancellable = true)
    private void polydium$buildLinesIdentityPose(List<DrawableGizmoPrimitives.Line> lines, CameraRenderState camera, Matrix4fc modelViewMatrix, boolean opaque, CallbackInfo ci) {
        if (lines.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = this.poseStack.last();
    //?} else {
    /*@Inject(method = "renderLines", at = @At("HEAD"), cancellable = true)
    private void polydium$buildLinesIdentityPose(PoseStack poseStack, MultiBufferSource bufferSource, CameraRenderState camera, /^? if >= 26.1 {^/Matrix4fc/^?} else {^//^Matrix4f^//^?}^/ modelViewMatrix, CallbackInfo ci) {
        if (this.lines.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = poseStack.last();
    *///?}
        if (!POLYDIUM_IDENTITY.equals(pose.pose())) {
            return;
        }
        ci.cancel();

        //? if >= 26.3 {
        VertexConsumer builder = this.getVertexBuilder(opaque ? RenderTypes.lines() : RenderTypes.linesTranslucentNoDepthWrite());
        //?} else if >= 26.2 {
        /*VertexConsumer builder = this.getVertexBuilder(opaque ? RenderTypes.lines() : RenderTypes.linesTranslucent());
        *///?} else {
        /*VertexConsumer builder = bufferSource.getBuffer(this.opaque ? RenderTypes.lines() : RenderTypes.linesTranslucent());
        *///?}
        boolean plain = builder.getClass() == BufferBuilder.class;
        double camX = camera.pos.x();
        double camY = camera.pos.y();
        double camZ = camera.pos.z();
        float[] p = this.polydium$line;

        //? if >= 26.2 {
        for (DrawableGizmoPrimitives.Line line : lines) {
            Vec3 start = line.start();
            Vec3 end = line.end();
            int color = line.color();
            float width = line.width();
        //?} else {
        /*for (Object element : this.lines) {
            GizmoLineAccessor line = (GizmoLineAccessor) element;
            Vec3 start = line.polydium$start();
            Vec3 end = line.polydium$end();
            int color = line.polydium$color();
            float width = line.polydium$width();
        *///?}
            p[0] = (float) (start.x() - camX);
            p[1] = (float) (start.y() - camY);
            p[2] = (float) (start.z() - camZ);
            p[3] = (float) (end.x() - camX);
            p[4] = (float) (end.y() - camY);
            p[5] = (float) (end.z() - camZ);
            if (!DebugLineClip.clip(modelViewMatrix, p)) {
                continue;
            }

            float normalX = p[3] - p[0];
            float normalY = p[4] - p[1];
            float normalZ = p[5] - p[2];
            if (plain && DebugLineClip.identityExact(p)) {
                builder.addVertex(p[0], p[1], p[2]).setNormal(pose, normalX, normalY, normalZ).setColor(color).setLineWidth(width);
                builder.addVertex(p[3], p[4], p[5]).setNormal(pose, normalX, normalY, normalZ).setColor(color).setLineWidth(width);
            } else {
                builder.addVertex(pose, p[0], p[1], p[2]).setNormal(pose, normalX, normalY, normalZ).setColor(color).setLineWidth(width);
                builder.addVertex(pose, p[3], p[4], p[5]).setNormal(pose, normalX, normalY, normalZ).setColor(color).setLineWidth(width);
            }
        }
    }
}
//?}
