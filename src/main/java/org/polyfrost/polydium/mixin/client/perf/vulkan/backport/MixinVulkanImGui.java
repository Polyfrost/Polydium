package org.polyfrost.polydium.mixin.client.perf.vulkan.backport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
    "com.moulberry.flashback.editor.ui.CustomImGuiImplGl3",
    "com.moulberry.flashback.FramebufferUtils",
    "com.moulberry.flashback.editor.ui.windows.ExportDoneWindow$FinishedExportEntry",
    "com.moulberry.flashback.exporting.SaveableFramebuffer",
    "com.moulberry.axiom.editor.CustomImGuiImplGl3",
    "com.moulberry.axiom.editor.EditorUI",
    "com.moulberry.axiom.editor.ImageReferenceWindows",
    "com.moulberry.axiom.editor.BlueprintPreview",
    "com.moulberry.axiom.editor.widgets.FalloffWidget",
    "com.moulberry.axiom.editor.windows.clipboard.BlueprintBrowserWindow",
    "com.moulberry.axiom.clipboard.ClipboardObject$Anonymous",
    "com.moulberry.axiom.clipboard.ClipboardObject$FromBlueprint",
    "com.moulberry.axiom.core_rendering.AxiomRenderer",
    "com.moulberry.axiom.hooks.WorldRenderHook",
    "com.moulberry.axiom.render.BlockRenderCache",
    "com.moulberry.axiom.render.annotations.Annotations",
    "com.moulberry.axiom.utils.AutoCleaningDynamicTexture",
    "com.moulberry.axiom.utils.FramebufferUtils",
    "com.moulberry.axiom.utils.BoundFramebufferBackup"
}, remap = false)
public class MixinVulkanImGui {
}
