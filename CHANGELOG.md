## Unreleased changes
- Fixed automatic Vulkan switching never turning on, since the GPU check ran too early in startup to see the GPU
- Fixed text caching falling behind when a frame draws more text than the cache holds, such as rooms full of signs
- Dropped items reuse their render state between frames
- Reduced per-frame allocations with Entity Culling's block entity checks

## 1.3.1
- Properly fix unlimited fps