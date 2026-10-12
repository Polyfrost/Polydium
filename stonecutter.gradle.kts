plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3-fabric"

stonecutter handlers {
    inherit("json5", "json")
}

val renderpearlRenames = mapOf(
    "renderpearl.api.GpuFormat" to "blaze3d.GpuFormat",
    "renderpearl.api.pipeline.BindGroupLayout" to "blaze3d.pipeline.BindGroupLayout",
    "renderpearl.api.buffers.GpuBuffer" to "blaze3d.buffers.GpuBuffer",
    "renderpearl.api.buffers.GpuBufferSlice" to "blaze3d.buffers.GpuBufferSlice",
    "renderpearl.api.commands.CommandEncoder" to "blaze3d.systems.CommandEncoder",
    "renderpearl.api.commands.GpuFence" to "blaze3d.buffers.GpuFence",
    "renderpearl.api.commands.RenderPass" to "blaze3d.systems.RenderPass",
    "renderpearl.api.commands.RenderPassDescriptor" to "blaze3d.systems.RenderPassDescriptor",
    "renderpearl.api.device.GpuDevice" to "blaze3d.systems.GpuDevice",
    "renderpearl.api.device.GpuSurface" to "blaze3d.systems.GpuSurface",
    "renderpearl.api.device.SurfaceException" to "blaze3d.systems.SurfaceException",
    "renderpearl.api.pipeline.BlendFunction" to "blaze3d.pipeline.BlendFunction",
    "renderpearl.api.pipeline.ColorTargetState" to "blaze3d.pipeline.ColorTargetState",
    "renderpearl.api.pipeline.CompareOp" to "blaze3d.platform.CompareOp",
    "renderpearl.api.pipeline.CompiledRenderPipeline" to "blaze3d.pipeline.CompiledRenderPipeline",
    "renderpearl.api.pipeline.DepthStencilState" to "blaze3d.pipeline.DepthStencilState",
    "renderpearl.api.pipeline.IndexType" to "blaze3d.IndexType",
    "renderpearl.api.pipeline.PrimitiveTopology" to "blaze3d.PrimitiveTopology",
    "renderpearl.api.pipeline.RenderPipeline" to "blaze3d.pipeline.RenderPipeline",
    "renderpearl.api.pipeline.ShaderSource" to "blaze3d.shaders.ShaderSource",
    "renderpearl.api.pipeline.ShaderType" to "blaze3d.shaders.ShaderType",
    "renderpearl.api.textures.FilterMode" to "blaze3d.textures.FilterMode",
    "renderpearl.api.textures.GpuSampler" to "blaze3d.textures.GpuSampler",
    "renderpearl.api.textures.GpuTexture" to "blaze3d.textures.GpuTexture",
    "renderpearl.api.textures.GpuTextureView" to "blaze3d.textures.GpuTextureView",
    "renderpearl.api.vertex.VertexFormat" to "blaze3d.vertex.VertexFormat",
    "renderpearl.api.vertex.VertexFormatElement" to "blaze3d.vertex.VertexFormatElement",
    "renderpearl.backend.api.GpuDeviceBackend" to "blaze3d.systems.GpuDeviceBackend",
    "renderpearl.backend.api.RenderPassBackend" to "blaze3d.systems.RenderPassBackend",
    "renderpearl.backend.opengl.DirectStateAccess" to "blaze3d.opengl.DirectStateAccess",
    "renderpearl.backend.opengl.GlCommandEncoder" to "blaze3d.opengl.GlCommandEncoder",
    "renderpearl.backend.opengl.GlDevice" to "blaze3d.opengl.GlDevice",
    "renderpearl.backend.opengl.GlBuffer" to "blaze3d.opengl.GlBuffer",
    "renderpearl.backend.opengl.GlFence" to "blaze3d.opengl.GlFence",
    "renderpearl.backend.opengl.GlRenderPass" to "blaze3d.opengl.GlRenderPass",
    "renderpearl.backend.opengl.GlStateManager" to "blaze3d.opengl.GlStateManager",
    "renderpearl.backend.vulkan.Destroyable" to "blaze3d.vulkan.Destroyable",
    "renderpearl.backend.vulkan.DestructionQueue" to "blaze3d.vulkan.DestructionQueue",
    "renderpearl.backend.vulkan.VulkanCommandEncoder" to "blaze3d.vulkan.VulkanCommandEncoder",
    "renderpearl.backend.vulkan.VulkanDebug" to "blaze3d.vulkan.VulkanDebug",
    "renderpearl.backend.vulkan.VulkanDevice" to "blaze3d.vulkan.VulkanDevice",
    "renderpearl.backend.vulkan.VulkanGpuBuffer" to "blaze3d.vulkan.VulkanGpuBuffer",
    "renderpearl.backend.vulkan.VulkanGpuSurface" to "blaze3d.vulkan.VulkanGpuSurface",
    "renderpearl.backend.vulkan.VulkanGpuTexture" to "blaze3d.vulkan.VulkanGpuTexture",
    "renderpearl.backend.vulkan.VulkanGpuTextureView" to "blaze3d.vulkan.VulkanGpuTextureView",
    "renderpearl.backend.vulkan.VulkanQueue" to "blaze3d.vulkan.VulkanQueue",
    "renderpearl.backend.vulkan.VulkanRenderPass" to "blaze3d.vulkan.VulkanRenderPass",
    "renderpearl.backend.vulkan.VulkanRenderPipeline" to "blaze3d.vulkan.VulkanRenderPipeline",
    "renderpearl.backend.vulkan.VulkanTransientMemory" to "blaze3d.vulkan.VulkanTransientMemory",
    "renderpearl.backend.vulkan.VulkanUtils" to "blaze3d.vulkan.VulkanUtils",
    "renderpearl.backend.vulkan.checkpoints.CheckpointExtension" to "blaze3d.vulkan.checkpoints.CheckpointExtension",
    "renderpearl.frontend.shaders.GlslCompiler" to "blaze3d.vulkan.glsl.GlslCompiler",
    "renderpearl.util.ShaderCompileException" to "blaze3d.vulkan.glsl.ShaderCompileException",
)

stonecutter parameters {
    replacements {
        string(eval(current.version, ">= 1.21.11"), "identifier") {
            replace("ResourceLocation", "Identifier")
        }
        string(eval(current.version, "< 26.3")) {
            for ((new, old) in renderpearlRenames) {
                replace("com.mojang.$new", "com.mojang.$old")
                replace("com/mojang/${new.replace('.', '/')}", "com/mojang/${old.replace('.', '/')}")
            }
        }
    }
}

stonecutter tasks {
    order("runClient")
    order("publishModrinth")
}
