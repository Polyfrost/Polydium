## 1.4.23
- fix: crash on render types without OIT pipelines
- fix: crash from null GLFW input handle on Vulkan
- fix: Vulkan crash recheck no longer treats native out-of-memory as a failed start
- perf: skip GL fence poll for in-flight staged buffer recycles
- perf: cache Shield Status shield check per model

## 1.4.22
- fix: crash with Essential on 26.3
- feat: Experimental Borderless option on 26.3 (Windows). Makes borderless fullscreen exactly monitor-sized so Windows stops compositing it, for higher FPS. Off by default

## 1.4.21
- perf: cut per-frame allocations (nametag sorting, entity names, text, armor stands, Vulkan render passes) for fewer GC stutters
- fix: compat with BetterClouds

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