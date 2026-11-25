package org.polyfrost.polydium.client;

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

    public static boolean isLegacy(String source) {
        return source.contains("#moj_import");
    }

    //? if >= 26.3 {
    private static final Logger LOGGER = LoggerFactory.getLogger("Polydium");

    public static Map<Identifier, Resource> replaceLegacy(Map<Identifier, Resource> files, ResourceManager manager) {
        Map<Identifier, Resource> result = null;
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Resource top = entry.getValue();
            if ("vanilla".equals(top.sourcePackId()) || !isLegacy(top)) continue;
            Identifier location = entry.getKey();
            Resource fallback = fallback(manager.getResourceStack(location));
            if (fallback == null) continue;
            LOGGER.warn("Resource pack {} has a pre-26.3 shader {}, using the one from {} instead", top.sourcePackId(), location, fallback.sourcePackId());
            if (result == null) result = new HashMap<>(files);
            result.put(location, fallback);
        }
        return result == null ? files : result;
    }

    private static Resource fallback(List<Resource> stack) {
        for (int i = stack.size() - 2; i >= 0; i--) {
            Resource resource = stack.get(i);
            if (!isLegacy(resource)) return resource;
        }
        return null;
    }

    private static boolean isLegacy(Resource resource) {
        try {
            return isLegacy(resource.readAllAsString());
        } catch (IOException e) {
            return false;
        }
    }
    //?}
}
