## Unreleased changes
- Fixed automatic Vulkan switching never turning on, since the GPU check ran too early in startup to see the GPU
- Fixed text caching falling behind when a frame draws more text than the cache holds, such as rooms full of signs
- Dropped items reuse their render state between frames
- Reduced per-frame allocations with Entity Culling's block entity checks
- Fixed a crash on the Vulkan backend with mods that rely on OpenGL keeping the previous transforms bound, such as Teamviewer (Hoplite)
- Fixed VRAM growing with every Iris shader or resource reload on 26.3

## 1.3.1
- Properly fix unlimited fps