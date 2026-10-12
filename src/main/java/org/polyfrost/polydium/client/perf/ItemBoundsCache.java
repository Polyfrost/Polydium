package org.polyfrost.polydium.client.perf;

//? if >= 1.21.8 {
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.impl.client.renderer.LayerRenderStateExtension;
//?} else {
/*import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.loader.api.FabricLoader;
*///?}
import net.minecraft.client.renderer.item.ItemStackRenderState;
//? if >= 26.1 {
import net.minecraft.client.resources.model.cuboid.ItemTransform;
//?} else {
/*import net.minecraft.client.renderer.block.model.ItemTransform;
*///?}
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

public final class ItemBoundsCache {
    private static final int MAX_ENTRIES = 4096;
    private static final Map<Key, AABB> BOUNDS = new HashMap<>();
    private static final Key PROBE = new Key();
    private static boolean fabricBroken;
    //? if < 26.1 {
    /*private static final boolean SODIUM = FabricLoader.getInstance().isModLoaded("sodium");
    *///?}

    private ItemBoundsCache() {
    }

    public static boolean isPlainLayer(ItemStackRenderState.LayerRenderState layer) {
        if (fabricBroken) {
            return false;
        }
        try {
            //? if >= 26.1 {
            if (!((Object) layer instanceof LayerRenderStateExtension extension)) {
                return true;
            }
            MutableMesh mesh = extension.fabric_getMutableMesh();
            return mesh == null || mesh.size() == 0;
            //?} else {
            /*MutableMesh mesh;
            if ((Object) layer instanceof net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState indigo) {
                mesh = indigo.fabric_getMutableMesh();
            //? if >= 1.21.8 {
            } else if (SODIUM && (Object) layer instanceof net.caffeinemc.mods.sodium.client.render.frapi.render.AccessLayerRenderState sodium) {
                mesh = sodium.fabric_getMutableMesh();
            //?}
            } else {
                return !((Object) layer instanceof FabricLayerRenderState);
            }
            return mesh == null || mesh.size() == 0;
            *///?}
        } catch (LinkageError e) {
            fabricBroken = true;
            return false;
        }
    }

    public static AABB find(Supplier<Vector3fc[]> extents, ItemTransform itemTransform, boolean leftHand, Matrix4f localTransform) {
        PROBE.set(extents, itemTransform, leftHand, localTransform);
        return BOUNDS.get(PROBE);
    }

    public static void store(AABB bounds, Supplier<Vector3fc[]> extents, ItemTransform itemTransform, boolean leftHand, Matrix4f localTransform) {
        if (BOUNDS.size() >= MAX_ENTRIES) {
            BOUNDS.clear();
        }
        Key key = new Key();
        key.set(extents, itemTransform, leftHand, localTransform);
        BOUNDS.put(key, bounds);
    }

    private static final class Key {
        private final float[] transform = new float[16];
        private Supplier<Vector3fc[]> extents;
        private ItemTransform itemTransform;
        private boolean leftHand;
        private int hash;

        private void set(Supplier<Vector3fc[]> extents, ItemTransform itemTransform, boolean leftHand, Matrix4f localTransform) {
            this.extents = extents;
            this.itemTransform = itemTransform;
            this.leftHand = leftHand;
            localTransform.get(this.transform);
            int value = System.identityHashCode(extents);
            value = 31 * value + System.identityHashCode(itemTransform);
            value = 31 * value + Boolean.hashCode(leftHand);
            for (float element : this.transform) {
                value = 31 * value + Float.floatToRawIntBits(element);
            }
            this.hash = value;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key that) || this.extents != that.extents || this.itemTransform != that.itemTransform || this.leftHand != that.leftHand) {
                return false;
            }
            for (int i = 0; i < 16; i++) {
                if (Float.floatToRawIntBits(this.transform[i]) != Float.floatToRawIntBits(that.transform[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }
}
//?}
