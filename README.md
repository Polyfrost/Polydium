# Polydium

Miscellaneous client-side optimization mod built by Polyfrost. On average, adding Polydium alongside the [OneClient modpack](https://modrinth.com/modpack/oneclient-modpack) **improves average FPS by 20%.**

The magic of this mod is how it works with other mods. Polydium works the best alongside Sodium, using its infrastructure to improve performance in mods such as 3D Skin Layers.

Notable features:
- Global text width caching
- Occlusion culling, applying only intelligently when it is needed
- Batched particle rendering, writing whole particle layers straight into the vertex buffer
- Particle section culling, skipping particles in chunk sections Sodium has already culled
- Faster entity, item, and text vertex writing through Sodium's vertex pipeline
- GUI item baking, redrawing animated items only when their animation actually moves
- Deferred sky, drawing the sky after terrain so hidden sky pixels are skipped
- Cached block entity lighting, entity cull boxes, and entity shadow lookups
- Faster startup and resource reloads through shader, pipeline, and resource pack caching
- Optimizations for the new Vulkan backend
- F3 debug screen caching, refreshing entries on an interval instead of every frame
