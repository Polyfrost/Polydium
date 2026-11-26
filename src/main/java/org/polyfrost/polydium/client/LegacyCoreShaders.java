package org.polyfrost.polydium.client;

import java.util.regex.Pattern;

//? if >= 26.3 {
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//?}

public final class LegacyCoreShaders {
    private LegacyCoreShaders() {
    }

    private static final Pattern LAYOUT_LOCATION = Pattern.compile("layout\\s*\\(\\s*location");

    public static boolean isLegacy(String path, String source) {
        if (source.contains("#moj_import")) return true;
        if (path.endsWith(".glsl")) return source.contains("#version");
        if (path.endsWith(".vsh") || path.endsWith(".fsh")) return !LAYOUT_LOCATION.matcher(source).find();
        return false;
    }

    //? if >= 26.3 {
    private static final Logger LOGGER = LoggerFactory.getLogger("Polydium");

    public static Map<Identifier, Resource> replaceLegacy(Map<Identifier, Resource> files, ResourceManager manager) {
        Map<Identifier, Resource> result = null;
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier location = entry.getKey();
            Resource top = entry.getValue();
            if ("vanilla".equals(top.sourcePackId()) || !isLegacy(location, top)) continue;
            Resource fallback = fallback(location, manager.getResourceStack(location));
            if (fallback == null) continue;
            LOGGER.warn("Resource pack {} has a pre-26.3 shader {}, using the one from {} instead", top.sourcePackId(), location, fallback.sourcePackId());
            if (result == null) result = new HashMap<>(files);
            result.put(location, fallback);
        }
        return result == null ? files : result;
    }

    private static Resource fallback(Identifier location, List<Resource> stack) {
        for (int i = stack.size() - 2; i >= 0; i--) {
            Resource resource = stack.get(i);
            if (!isLegacy(location, resource)) return resource;
        }
        return null;
    }

    private static boolean isLegacy(Identifier location, Resource resource) {
        try {
            return isLegacy(location.getPath(), resource.readAllAsString());
        } catch (IOException e) {
            return false;
        }
    }
    //?}
}
