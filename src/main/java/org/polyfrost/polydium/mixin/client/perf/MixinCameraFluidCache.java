package org.polyfrost.polydium.mixin.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
//? if >= 26.1 {
import net.minecraft.client.renderer.Projection;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
//? if < 1.21.11
/*import net.minecraft.world.level.BlockGetter;*/
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.polyfrost.polydium.client.perf.PerfClock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Camera.class, priority = 999)
public abstract class MixinCameraFluidCache {
    @Unique
    private static final int NEAR_PLANE_POINTS = 5;

    @Shadow
    private boolean initialized;
    @Shadow
    private /*? if >= 1.21.11 {*/Level/*?} else {*//*BlockGetter*//*?}*/ level;
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

    @Shadow
    public abstract Camera.NearPlane getNearPlane(float fov);
    //?} else {
    /*@Shadow
    public abstract Camera.NearPlane getNearPlane();
    *///?}

    @Unique
    private final BlockPos.MutableBlockPos polydium$checkPos = new BlockPos.MutableBlockPos();
    @Unique
    private long polydium$fluidFrame = -1L;
    @Unique
    private long polydium$fluidTick;
    @Unique
    private boolean polydium$fluidInitialized;
    @Unique
    private /*? if >= 1.21.11 {*/Level/*?} else {*//*BlockGetter*//*?}*/ polydium$fluidLevel;
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

    @Overwrite
    public FogType getFluidInCamera() {
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
        FogType fluid = this.polydium$findFluid();
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

    @Unique
    private FogType polydium$findFluid() {
        if (!this.initialized)
            return FogType.NONE;

        FluidState cameraFluid = this.level.getFluidState(this.blockPosition);

        if (cameraFluid.is(FluidTags.WATER) && this.position.y < this.blockPosition.getY() + this.polydium$height(cameraFluid, this.blockPosition))
            return FogType.WATER;

        CameraNearPlaneAccessor  plane    = (CameraNearPlaneAccessor) this.polydium$nearPlane();
        Vec3                     forward  = plane.polydium$forward();
        Vec3                     left     = plane.polydium$left();
        Vec3                     up       = plane.polydium$up();
        double                   topX     = forward.x + up.x;
        double                   topY     = forward.y + up.y;
        double                   topZ     = forward.z + up.z;
        double                   bottomX  = forward.x + -up.x;
        double                   bottomY  = forward.y + -up.y;
        double                   bottomZ  = forward.z + -up.z;
        BlockPos.MutableBlockPos checkPos = this.polydium$checkPos.set(this.blockPosition);
        FluidState               fluid    = cameraFluid;
        BlockState               block    = null;

        for (int point = 0; point < NEAR_PLANE_POINTS; point ++) {
            double x = this.position.x + switch (point) {
                case 0 -> forward.x;
                case 1 -> topX + left.x;
                case 2 -> topX + -left.x;
                case 3 -> bottomX + left.x;
                default -> bottomX + -left.x;
            };
            double y = this.position.y + switch (point) {
                case 0 -> forward.y;
                case 1 -> topY + left.y;
                case 2 -> topY + -left.y;
                case 3 -> bottomY + left.y;
                default -> bottomY + -left.y;
            };
            double z = this.position.z + switch (point) {
                case 0 -> forward.z;
                case 1 -> topZ + left.z;
                case 2 -> topZ + -left.z;
                case 3 -> bottomZ + left.z;
                default -> bottomZ + -left.z;
            };
            int blockX = Mth.floor(x);
            int blockY = Mth.floor(y);
            int blockZ = Mth.floor(z);

            if (blockX != checkPos.getX() || blockY != checkPos.getY() || blockZ != checkPos.getZ()) {
                checkPos.set(blockX, blockY, blockZ);
                fluid = this.level.getFluidState(checkPos);
                block = null;
            }

            if (fluid.is(FluidTags.LAVA)) {
                if (y <= this.polydium$height(fluid, checkPos) + checkPos.getY())
                    return FogType.LAVA;

                continue;
            }

            if (block == null)
                block = this.level.getBlockState(checkPos);

            if (block.is(Blocks.POWDER_SNOW))
                return FogType.POWDER_SNOW;
        }

        return FogType.NONE;
    }

    @Unique
    private Camera.NearPlane polydium$nearPlane() {
        //? if >= 26.1 {
        return this.getNearPlane(this.minecraft.options.fov().get().intValue());
        //?} else {
        /*return this.getNearPlane();
        *///?}
    }

    @Unique
    private float polydium$height(FluidState fluid, BlockPos pos) {
        //? if >= 26.3 {
        return fluid.getHeightForCamera(this.level, pos);
        //?} else {
        /*return fluid.getHeight(this.level, pos);
        *///?}
    }
}
//?}
