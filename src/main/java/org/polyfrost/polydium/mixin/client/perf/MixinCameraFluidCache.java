package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
//? if >= 26.1 {
import net.minecraft.client.renderer.Projection;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Camera.class)
public class MixinCameraFluidCache {
    @Shadow
    private boolean initialized;
    @Shadow
    private Level level;
    @Shadow
    private Vec3 position;
    @Shadow
    @Final
    private BlockPos.MutableBlockPos blockPosition;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    @Final
    private Vector3f up;
    @Shadow
    @Final
    private Vector3f left;
    //? if >= 26.1 {
    @Shadow
    @Final
    private Projection projection;
    @Shadow
    @Final
    private Minecraft minecraft;
    //?}

    @Unique
    private long polydium$fluidFrame = -1L;
    @Unique
    private long polydium$fluidTick;
    @Unique
    private boolean polydium$fluidInitialized;
    @Unique
    private Level polydium$fluidLevel;
    @Unique
    private Vec3 polydium$fluidPosition;
    @Unique
    private long polydium$fluidBlockPos;
    @Unique
    private final Vector3f polydium$fluidForwards = new Vector3f();
    @Unique
    private final Vector3f polydium$fluidUp = new Vector3f();
    @Unique
    private final Vector3f polydium$fluidLeft = new Vector3f();
    @Unique
    private float polydium$fluidWidth;
    @Unique
    private float polydium$fluidHeight;
    @Unique
    private float polydium$fluidZNear;
    @Unique
    private int polydium$fluidFov;
    @Unique
    private FogType polydium$fluid;

    @WrapMethod(method = "getFluidInCamera")
    private FogType polydium$cacheFluid(Operation<FogType> original) {
        long frame = PerfClock.frame;
        long tick = PerfClock.tick;
        long blockPos = this.blockPosition.asLong();
        //? if >= 26.1 {
        float width = this.projection.width();
        float height = this.projection.height();
        float zNear = this.projection.zNear();
        int fov = this.minecraft.options.fov().get();
        //?} else {
        /*Minecraft minecraft = Minecraft.getInstance();
        float width = minecraft.getWindow().getWidth();
        float height = minecraft.getWindow().getHeight();
        float zNear = 0.05F;
        int fov = minecraft.options.fov().get();
        *///?}
        if (this.polydium$fluidFrame == frame
            && this.polydium$fluidTick == tick
            && this.polydium$fluidInitialized == this.initialized
            && this.polydium$fluidLevel == this.level
            && this.polydium$fluidPosition == this.position
            && this.polydium$fluidBlockPos == blockPos
            && this.polydium$fluidWidth == width
            && this.polydium$fluidHeight == height
            && this.polydium$fluidZNear == zNear
            && this.polydium$fluidFov == fov
            && this.polydium$fluidForwards.equals(this.forwards)
            && this.polydium$fluidUp.equals(this.up)
            && this.polydium$fluidLeft.equals(this.left)) {
            return this.polydium$fluid;
        }
        FogType fluid = original.call();
        this.polydium$fluidFrame = frame;
        this.polydium$fluidTick = tick;
        this.polydium$fluidInitialized = this.initialized;
        this.polydium$fluidLevel = this.level;
        this.polydium$fluidPosition = this.position;
        this.polydium$fluidBlockPos = blockPos;
        this.polydium$fluidWidth = width;
        this.polydium$fluidHeight = height;
        this.polydium$fluidZNear = zNear;
        this.polydium$fluidFov = fov;
        this.polydium$fluidForwards.set(this.forwards);
        this.polydium$fluidUp.set(this.up);
        this.polydium$fluidLeft.set(this.left);
        this.polydium$fluid = fluid;
        return fluid;
    }
}
//?}
