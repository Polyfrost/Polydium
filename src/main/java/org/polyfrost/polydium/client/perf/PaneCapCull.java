//~ identifier
package org.polyfrost.polydium.client.perf;

//? if >= 1.21.1 {
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.LoggerFactory;

public final class PaneCapCull {
    private static final String[] TEMPLATES = {
            "template_glass_pane_post", "template_glass_pane_side", "template_glass_pane_side_alt",
            "template_bars_cap", "template_bars_post_ends", "template_bars_side", "template_bars_side_alt",
            "iron_bars_cap", "iron_bars_post_ends", "iron_bars_side", "iron_bars_side_alt"
    };

    private static boolean logged;

    private PaneCapCull() {
    }

    public static Direction capFace(float fromX, float fromY, float fromZ, float toX, float toY, float toZ, Direction facing) {
        if (facing.getAxis() != Direction.Axis.Y || fromX != 7.0F || toX != 9.0F || fromZ != 7.0F || toZ != 9.0F) {
            return null;
        }
        if (fromY == 0.0F && toY == 16.0F) {
            return facing;
        }
        if (fromY == toY) {
            if (facing == Direction.UP && fromY >= 15.99F) {
                return Direction.UP;
            }
            if (facing == Direction.DOWN && fromY <= 0.01F) {
                return Direction.DOWN;
            }
        }
        return null;
    }

    public static Direction cullFace(float fromX, float fromY, float fromZ, float toX, float toY, float toZ, Direction facing) {
        Direction cap = capFace(fromX, fromY, fromZ, toX, toY, toZ, facing);
        return cap != null && vanillaModels() ? cap : null;
    }

    private static boolean vanillaModels() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }
        ResourceManager resources = minecraft.getResourceManager();
        for (String template : TEMPLATES) {
            String path = "models/block/" + template + ".json";
            String pack = resources.getResource(Identifier.withDefaultNamespace(path))
                    .map(resource -> resource.sourcePackId()).orElse("vanilla");
            if (!"vanilla".equals(pack)) {
                return standDown(path + " comes from pack " + pack);
            }
        }
        return true;
    }

    private static boolean standDown(String reason) {
        if (!logged) {
            logged = true;
            LoggerFactory.getLogger("polydium/perf").debug("PaneCapCull stands down: {}", reason);
        }
        return false;
    }
}
//?}
