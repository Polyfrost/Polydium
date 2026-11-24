package org.polyfrost.polydium.mixin.client.perf.vulkan;

//? if >= 1.21.11 && < 26.3 {
/*import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GLFW.class, remap = false)
public class MixinGlfwContextlessWindowInput {
    @ModifyVariable(method = {"glfwGetKey", "glfwGetMouseButton"}, at = @At("HEAD"), argsOnly = true)
    private static long polydium$gameWindowForNullHandle(long window) {
        return window == 0L ? polydium$gameWindow() : window;
    }

    @Unique
    private static long polydium$gameWindow() {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft == null ? null : minecraft.getWindow();
        return window == null ? 0L : window.handle();
    }
}
*///?}
