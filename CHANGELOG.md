## 1.4.20
- perf: improve performance of occlusion culling by splitting in parallel
- perf: transform debug line normal once per line
- fix: only sort quad draws in outline scissor groups
- fix: remove faulty optimization mixin
- fix: fall back to vanilla particle OIT pipeline for layers without one
- fix: only reset backend after an unfinished Vulkan start
- fix: restore Default backend after vanilla's forced OpenGL start
- fix: refuse to start Streamproof on Vulkan
- fix: switch to Vulkan after OpenGL driver load crash