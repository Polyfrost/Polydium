## 1.2.1
- Fixed the "Unlimited" FPS limit being capped at 260 on 1.21.11 and below
- Fixed a crash on launch and broken post-processing effects with VulkanMod
- The VulkanMod notice is no longer shown on Minecraft versions where Polydium doesn't have a Vulkan backend
- Polydium can now be disabled entirely using the `-Dpolydium.disableAll=true` JVM argument
- Fixed game inputs being bricked after a resource reload if Hide Reload Overlay is enabled with kennytvs-epic-force-close-loading-screen-mod-for-fabric installed
- Fixed a crash with the Not Enough FPS mod
- Fixed a crash on Vulkan with GPUs that lack push descriptor support, such as older AMD APUs
