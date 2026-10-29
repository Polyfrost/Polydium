package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
//? if >= 26.3 {
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.util.TextureViewAndSampler;
//?}

public final class TextureList extends AbstractList<PreparedRenderType.Texture> implements RandomAccess {

    private final PreparedRenderType.Texture[] textures;
    private final int                          hash;
    //? if >= 26.3 {
    private final TextureViewAndSampler[]      bindings;
    //?}

    public TextureList(PreparedRenderType.Texture[] textures) {
        this.textures = textures;
        this.hash     = Arrays.hashCode(textures);
        //? if >= 26.3 {
        this.bindings = new TextureViewAndSampler[textures.length];
        for (int index = 0; index < textures.length; index++) {
            PreparedRenderType.Texture texture = textures[index];
            if (texture.textureView() != null && texture.sampler() != null) {
                this.bindings[index] = new TextureViewAndSampler(texture.textureView(), texture.sampler());
            }
        }
        //?}
    }

    //? if >= 26.3 {
    public TextureViewAndSampler binding(String name, GpuTextureView view, GpuSampler sampler) {
        PreparedRenderType.Texture[] textures = this.textures;
        for (int index = 0; index < textures.length; index++) {
            PreparedRenderType.Texture texture = textures[index];
            if (texture.name() == name && texture.textureView() == view && texture.sampler() == sampler) {
                return this.bindings[index];
            }
        }
        return null;
    }

    public interface Binder {
        boolean polydium$bindTexture(String name, TextureViewAndSampler binding);
    }
    //?}

    @Override
    public PreparedRenderType.Texture get(int index) {
        return this.textures[index];
    }

    @Override
    public int size() {
        return this.textures.length;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;

        if (other instanceof TextureList list)
            return list.hash == this.hash && Arrays.equals(this.textures, list.textures);

        return super.equals(other);
    }
}
//?}
