package org.polyfrost.polydium.mixin.client.perf;

//? if >= 26.3 {
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Font.class)
public interface FontProviderAccessor {
    @Accessor("provider")
    Font.Provider polydium$provider();
}
//?}
