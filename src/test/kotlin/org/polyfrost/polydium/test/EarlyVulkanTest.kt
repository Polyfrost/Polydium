package org.polyfrost.polydium.test

import net.fabricmc.loader.api.FabricLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.early.EarlyVulkan
import org.polyfrost.polydium.early.EarlyVulkanPreLaunch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant

class EarlyVulkanTest {

    private val gameDir = FabricLoader.getInstance().gameDir
    private val polydiumDir = gameDir.resolve("polydium")
    private val marker = polydiumDir.resolve("auto-vulkan")
    private val options = gameDir.resolve("options.txt")
    private val yosbrOptions = gameDir.resolve("config/yosbr/options.txt")
    private val configManagerOptions = gameDir.resolve("config/modpack_defaults/options.txt")
    private val defaultOptions = gameDir.resolve("config/defaultoptions/options.txt")
    private val extraOptions = gameDir.resolve("config/defaultoptions/extra/options.txt")
    private val extraGraphicsApi = gameDir.resolve("config/defaultoptions/extra/polydium/graphics-api")
    private val yosbrGraphicsApi = gameDir.resolve("config/yosbr/polydium/graphics-api")
    private val yosbrNestedGraphicsApi = gameDir.resolve("config/yosbr/config/defaultoptions/extra/polydium/graphics-api")
    private val irisConfig = gameDir.resolve("config/iris.properties")

    @BeforeEach
    @AfterEach
    fun resetInstall() {
        assumeTrue(gameDir.endsWith("test-run"), "resets the game dir, so it only runs in the test-run dir")
        //? if >= 26.2
        EarlyVulkan.removeCreatedOptions()
        polydiumDir.toFile().deleteRecursively()
        Files.deleteIfExists(options)
        System.setProperty("polydium.vulkanEligible", "true")
        gameDir.resolve("config/yosbr").toFile().deleteRecursively()
        Files.deleteIfExists(configManagerOptions)
        gameDir.resolve("config/defaultoptions").toFile().deleteRecursively()
        System.clearProperty("polydium.disable")
        gameDir.resolve("crash-reports").toFile().deleteRecursively()
        Files.deleteIfExists(gameDir.resolve("logs/2026-11-12-2.log.gz"))
        Files.deleteIfExists(gameDir.resolve("logs/2026-11-12-3.log.gz"))
        Files.deleteIfExists(irisConfig)
        Files.deleteIfExists(gameDir.resolve("logs/debug-2026-11-12-1.log.gz"))
        EarlyVulkan::class.java.getDeclaredField("vulkanAtStartup").apply { isAccessible = true }.set(null, null)
        //? if >= 26.2 {
        EarlyVulkan::class.java.getDeclaredField("irisLoaded").apply { isAccessible = true }.set(null, false)
        Files.deleteIfExists(irisConfig)
        //?}
    }

    @Test
    fun onlyVulkanChoiceSupersedesFailure() {
        EarlyVulkan.markFailed("test")

        EarlyVulkan.setGraphicsApi("default")
        assertTrue(EarlyVulkan.failed())
        EarlyVulkan.setGraphicsApi("opengl")
        assertTrue(EarlyVulkan.failed())

        EarlyVulkan.setGraphicsApi("vulkan")
        assertFalse(EarlyVulkan.failed())
        assertTrue(EarlyVulkan.switched())
        assertEquals("vulkan", EarlyVulkan.graphicsApi())
    }

    //? if >= 1.21.11 {
    private fun useRealGpuCheck() {
        assumeFalse(System.getProperty("os.name", "").startsWith("Mac") && System.getProperty("os.arch") == "aarch64")
        System.clearProperty("polydium.vulkanEligible")
    }

    @Test
    fun unknownGpuKeepsRetryMarker() {
        useRealGpuCheck()
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "done")

        EarlyVulkan()

        assertEquals("done", Files.readString(marker))
    }

    @Test
    fun foundGpuOverridesNegativeMarker() {
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "no")

        EarlyVulkan()

        assertEquals("", Files.readString(marker))
    }

    @Test
    fun negativeMarkerStandsWithoutGpu() {
        System.setProperty("polydium.vulkanEligible", "false")
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "no")
        val written = FileTime.from(Instant.now().minus(Duration.ofDays(1)))
        Files.setLastModifiedTime(marker, written)

        EarlyVulkan()

        assertEquals(written, Files.getLastModifiedTime(marker))
    }

    @Test
    fun staleGpuResultDecidesNothing() {
        useRealGpuCheck()
        EarlyVulkan.recordDiscreteGpu(true)
        Files.setLastModifiedTime(polydiumDir.resolve("discrete-gpu"), FileTime.from(Instant.now().minus(Duration.ofDays(31))))

        EarlyVulkan()

        assertFalse(Files.exists(marker))
    }

    @Test
    fun preLaunchDetectsGpuWhereEarlyVulkanCannot() {
        useRealGpuCheck()

        EarlyVulkanPreLaunch().onPreLaunch()

        assertTrue(EarlyVulkan.discreteGpuKnown())
    }

    @Test
    fun earlyGpuDetectionDecidesInTheSameLaunch() {
        useRealGpuCheck()

        EarlyVulkanPreLaunch().beforeMixinSelection()

        assertTrue(Files.exists(marker))
    }

    //? if < 26.2 {
    /*@Test
    fun gpuDetectionSelectsVulkanBeforeMixinDecision() {
        EarlyVulkanPreLaunch().beforeMixinSelection()

        assertEquals("vulkan", EarlyVulkan.graphicsApi())
        assertTrue(EarlyVulkan.switchPending())
        assertTrue(EarlyVulkan.vulkanAtStartup())
    }

    @Test
    fun lateGpuDetectionDoesNotChangeBackendAfterMixinDecision() {
        EarlyVulkanPreLaunch().onPreLaunch()

        assertTrue(EarlyVulkan.discreteGpuKnown())
        assertEquals("default", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(marker))
    }
    *///?}

    //? if >= 26.2 {
    @Test
    fun preLaunchFinishesRetryMarker() {
        useRealGpuCheck()
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "done")

        EarlyVulkanPreLaunch().onPreLaunch()

        assertNotEquals("done", Files.readString(marker))
    }
    //?}

    @Test
    fun freshInstallSelectsVulkanOnEligibleHardware() {
        EarlyVulkan()

        assertEquals("", Files.readString(marker))
        //? if >= 26.2 {
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"vulkan\""), Files.readAllLines(options))
        //?} else {
        /*assertEquals("vulkan", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(options))
        *///?}
    }

    @Test
    fun freshInstallStaysDefaultOnIneligibleHardware() {
        System.setProperty("polydium.vulkanEligible", "false")

        EarlyVulkan()

        assertEquals("no", Files.readString(marker))
        assertEquals("default", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(options))
    }

    @Test
    fun emptyOptionsCountAsFreshInstall() {
        Files.createFile(options)

        EarlyVulkan()

        assertEquals("", Files.readString(marker))
    }

    @Test
    fun packGraphicsApiBeatsAutomaticSwitch() {
        Files.createDirectories(extraGraphicsApi.parent)
        Files.writeString(extraGraphicsApi, "opengl")

        EarlyVulkan()

        assertEquals("opengl", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(marker))
        //? if >= 26.2 {
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
        //?}
    }

    @Test
    fun yosbrGraphicsApiBeatsAutomaticSwitch() {
        Files.createDirectories(yosbrGraphicsApi.parent)
        Files.writeString(yosbrGraphicsApi, "opengl")

        EarlyVulkan()

        assertEquals("opengl", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(marker))
    }

    @Test
    fun yosbrGraphicsApiBeatsDefaultOptions() {
        Files.createDirectories(yosbrGraphicsApi.parent)
        Files.writeString(yosbrGraphicsApi, "opengl")
        Files.createDirectories(extraGraphicsApi.parent)
        Files.writeString(extraGraphicsApi, "vulkan")

        EarlyVulkan()

        assertEquals("opengl", EarlyVulkan.graphicsApi())
    }

    @Test
    fun yosbrCopyOfDefaultOptionsGraphicsApiBeatsAutomaticSwitch() {
        Files.createDirectories(yosbrNestedGraphicsApi.parent)
        Files.writeString(yosbrNestedGraphicsApi, "opengl")

        EarlyVulkan()

        assertEquals("opengl", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(marker))
    }

    @Test
    fun uncleanPackOptionsAreNotSwitched() {
        Files.createDirectories(yosbrOptions.parent)
        Files.writeString(yosbrOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nstartedCleanly:false\n")

        EarlyVulkan()

        assertFalse(Files.exists(marker))
        assertEquals("default", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(options))
    }

    @Test
    fun uncleanDefaultOptionsHoldBackPackVulkanWithoutFailure() {
        Files.createDirectories(extraGraphicsApi.parent)
        Files.writeString(extraGraphicsApi, "vulkan")
        Files.createDirectories(defaultOptions.parent)
        Files.writeString(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nstartedCleanly:false\n")

        EarlyVulkan()

        assertFalse(Files.exists(options))
        assertFalse(EarlyVulkan.vulkanAtStartup())
        assertFalse(EarlyVulkan.failed())
        assertEquals("vulkan", EarlyVulkan.graphicsApi())
    }

    @Test
    fun uncleanCopyOfPackOptionsIsNotAFailedStart() {
        val pack = "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nstartedCleanly:false\n"
        Files.createDirectories(yosbrOptions.parent)
        Files.writeString(yosbrOptions, pack)
        Files.writeString(options, pack)
        EarlyVulkan.setGraphicsApi("vulkan")

        assertFalse(EarlyVulkan.vulkanAtStartup())
        assertFalse(EarlyVulkan.failed())
        assertEquals("vulkan", EarlyVulkan.graphicsApi())
    }

    @Test
    fun uncleanSavedOptionsAreAFailedStart() {
        Files.createDirectories(yosbrOptions.parent)
        Files.writeString(yosbrOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nstartedCleanly:false\n")
        Files.writeString(options, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nfov:1.0\nstartedCleanly:false\n")
        EarlyVulkan.setGraphicsApi("vulkan")

        assertFalse(EarlyVulkan.vulkanAtStartup())
        assertTrue(EarlyVulkan.failed())
        assertEquals("default", EarlyVulkan.graphicsApi())
    }

    @Test
    fun cleanDefaultOptionsLetVulkanStart() {
        Files.createDirectories(defaultOptions.parent)
        Files.writeString(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nstartedCleanly:true\n")
        EarlyVulkan.setGraphicsApi("vulkan")

        assertTrue(EarlyVulkan.vulkanAtStartup())
    }

    @Test
    fun savedGraphicsApiBeatsPackGraphicsApi() {
        Files.createDirectories(extraGraphicsApi.parent)
        Files.writeString(extraGraphicsApi, "opengl")
        EarlyVulkan.setGraphicsApi("vulkan")

        EarlyVulkan()

        assertEquals("vulkan", EarlyVulkan.graphicsApi())
    }

    private fun legacyFailure() {
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "failed")
        EarlyVulkan.setGraphicsApi("default")
    }

    @Test
    fun sodiumBlockedFailureRetriesOnce() {
        legacyFailure()

        EarlyVulkan.retrySodiumBlockedFailure()

        assertEquals("sodium", Files.readString(marker))
        EarlyVulkan()
        assertTrue(EarlyVulkan.switchPending())

        Files.writeString(marker, "failed")
        EarlyVulkan.setGraphicsApi("default")
        EarlyVulkan.retrySodiumBlockedFailure()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun currentFailureIsNotRetried() {
        EarlyVulkan.markFailed("test")
        EarlyVulkan.setGraphicsApi("default")
        assertFalse(Files.exists(polydiumDir.resolve("sodium-retry")))

        EarlyVulkan.retrySodiumBlockedFailure()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun failureIsGuardedWhenIrisRetryCannotBeWritten() {
        Files.createDirectories(polydiumDir.resolve("iris-retry"))
        EarlyVulkan.markFailed("test")
        EarlyVulkan.setGraphicsApi("default")

        EarlyVulkan.retrySodiumBlockedFailure()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun backendSavedBeforeFailureIsKept() {
        legacyFailure()
        Files.setLastModifiedTime(polydiumDir.resolve("graphics-api"), FileTime.from(Instant.now().minus(Duration.ofDays(2))))

        EarlyVulkan.retrySodiumBlockedFailure()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun backendPickedAfterFailureIsKept() {
        legacyFailure()
        Files.setLastModifiedTime(marker, FileTime.from(Instant.now().minus(Duration.ofMinutes(1))))

        EarlyVulkan.retrySodiumBlockedFailure()

        assertTrue(EarlyVulkan.failed())
    }

    private val inputCrash = listOf(
        "Description: Initializing game",
        "",
        "java.lang.RuntimeException: Exception while invoking method 'overlay'.",
        "\tat knot//com.hamarb123.macos_input_fixes.client.ModernFabricReflectionHelper\$MethodImpl.invoke(ModernFabricReflectionHelper.java:454)",
        "\tat knot//net.minecraft.client.Minecraft.scrollCallback(MinecraftMixin.java:109)",
        "\tat knot//org.lwjgl.system.JNI.invokeV(JNI.java:1722)",
        "\tat knot//org.lwjgl.sdl.SDLEvents.SDL_PumpEvents(SDLEvents.java:194)",
        "\tat knot//com.mojang.blaze3d.platform.SDLEventHandler.flushInputEvents(SDLEventHandler.java:62)",
        "\tat knot//net.minecraft.client.Minecraft.<init>(Minecraft.java:578)",
        "Caused by: java.lang.NullPointerException",
        "\tat java.base/java.lang.invoke.MethodHandle.invokeWithArguments(MethodHandle.java:735)",
        "\t... 10 more",
        "",
        "A detailed walkthrough of the error, its code path and all known details is as follows:",
        "\tat knot//com.mojang.blaze3d.vulkan.VulkanDevice.submit(VulkanDevice.java:1)",
    )

    private val vulkanCrash = listOf(
        "Description: Initializing game",
        "",
        "java.lang.IllegalStateException: vkCreateDevice failed",
        "\tat knot//com.mojang.renderpearl.backend.vulkan.VulkanBackend.createDevice(VulkanBackend.java:80)",
        "\tat knot//net.minecraft.client.Minecraft.<init>(Minecraft.java:500)",
        "",
        "A detailed walkthrough of the error, its code path and all known details is as follows:",
    )

    @Test
    fun crashesInsideTheEventPumpAreNotRenderCrashes() {
        assertFalse(EarlyVulkan.renderCrashReport(inputCrash))
        assertTrue(EarlyVulkan.renderCrashReport(vulkanCrash))
        assertFalse(EarlyVulkan.renderCrashReport(listOf(
            "\tat net.minecraft.client.MouseHandler.onScroll(MouseHandler.java:1)",
            "\tat org.lwjgl.glfw.GLFWScrollCallbackI.callback(GLFWScrollCallbackI.java:1)",
            "\tat org.lwjgl.glfw.GLFW.glfwPollEvents(GLFW.java:1)",
            "\tat com.mojang.blaze3d.systems.RenderSystem.pollEvents(RenderSystem.java:1)",
        )))

        assertTrue(EarlyVulkan.renderCrashReport(listOf(
            "\tat com.mojang.blaze3d.platform.Window.defaultErrorCallback(Window.java:1)",
            "\tat org.lwjgl.glfw.GLFWErrorCallbackI.callback(GLFWErrorCallbackI.java:1)",
            "\tat org.lwjgl.glfw.GLFW.glfwCreateWindow(GLFW.java:1)",
        )))

        val pump = StackTraceElement("org.lwjgl.sdl.SDLEvents", "SDL_PumpEvents", null, 0)
        val handler = StackTraceElement("com.mojang.blaze3d.platform.SDLEventHandler", "flushInputEvents", null, 0)
        val backend = StackTraceElement("com.mojang.renderpearl.backend.vulkan.VulkanBackend", "createDevice", null, 0)
        assertFalse(EarlyVulkan.renderCrash(RuntimeException().apply { stackTrace = arrayOf(pump, handler) }))
        assertTrue(EarlyVulkan.renderCrash(RuntimeException().apply { stackTrace = arrayOf(backend, pump) }))
    }

    private fun failAfterCrash(report: List<String>): Path {
        legacyFailure()
        val file = gameDir.resolve("crash-reports/crash-2026-11-12_13.14.30-client.txt")
        Files.createDirectories(file.parent)
        Files.write(file, report)
        Files.setLastModifiedTime(file, beforeLaunch(Duration.ofMinutes(4)))
        rotatedLog("2026-11-12-2.log.gz", beforeLaunch(Duration.ofSeconds(40)))
        Files.setLastModifiedTime(marker, beforeLaunch(Duration.ofSeconds(30)))
        return file
    }

    private val launchedAt = ProcessHandle.current().info().startInstant().get()

    private fun beforeLaunch(by: Duration) = FileTime.from(launchedAt.minus(by))

    private fun rotatedLog(name: String, at: FileTime) {
        val log = gameDir.resolve("logs/$name")
        Files.createDirectories(log.parent)
        Files.writeString(log, "")
        Files.setLastModifiedTime(log, at)
    }

    @Test
    fun reportFromAnEarlierLaunchKeepsFailure() {
        failAfterCrash(inputCrash)
        rotatedLog("2026-11-12-3.log.gz", beforeLaunch(Duration.ofMinutes(2)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun debugLogRotationIsNotAnotherLaunch() {
        failAfterCrash(inputCrash)
        rotatedLog("debug-2026-11-12-1.log.gz", beforeLaunch(Duration.ofSeconds(35)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun brokenFileInCrashDirDoesNotStopRecheck() {
        assumeFalse(System.getProperty("os.name").startsWith("Windows"))
        failAfterCrash(inputCrash)
        Files.createSymbolicLink(gameDir.resolve("crash-reports/crash-dangling-client.txt"), gameDir.resolve("missing"))

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun leftoverIrisConfigWithoutIrisDoesNotBlockRecheck() {
        failAfterCrash(inputCrash)
        Files.createDirectories(irisConfig.parent)
        Files.writeString(irisConfig, "enableShaders=true\nshaderPack=BSL.zip\n")

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun nonRenderStartupCrashRetriesVulkanOnce() {
        failAfterCrash(inputCrash)

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())

        EarlyVulkan.markFailed("test")
        EarlyVulkan.setGraphicsApi("default")
        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    private fun crashAfterUncleanStart(report: List<String>) {
        EarlyVulkan.setGraphicsApi("vulkan")
        Files.write(options, listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"vulkan\"", "startedCleanly:false"))
        Files.setLastModifiedTime(options, FileTime.from(Instant.now().minus(Duration.ofMinutes(1))))
        val file = gameDir.resolve("crash-reports/crash-2026-11-12_13.14.30-client.txt")
        Files.createDirectories(file.parent)
        Files.write(file, report)
    }

    @Test
    fun nonRenderCrashFromOlderBuildIsNotAFailedStart() {
        crashAfterUncleanStart(inputCrash)

        EarlyVulkan()

        assertTrue(EarlyVulkan.vulkanAtStartup())
        assertFalse(EarlyVulkan.failed())
        assertTrue(Files.readAllLines(options).contains("startedCleanly:true"))
    }

    @Test
    fun truncatedReportIsAFailedStart() {
        crashAfterUncleanStart(listOf("Description: Initializing game", "", "java.lang.NullPointerException"))

        EarlyVulkan()

        assertFalse(EarlyVulkan.vulkanAtStartup())
        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun logCompressedAfterFailureStillCountsAsOneLaunch() {
        failAfterCrash(inputCrash)
        rotatedLog("2026-11-12-2.log.gz", beforeLaunch(Duration.ofSeconds(27)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun failureFromBuildWithOldRecheckFileIsNotRechecked() {
        failAfterCrash(inputCrash)
        val old = polydiumDir.resolve("crash-recheck")
        Files.writeString(old, "")
        Files.setLastModifiedTime(old, FileTime.from(Files.getLastModifiedTime(marker).toInstant().plusSeconds(2)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun failureMarksOldRecheckFileForDowngrades() {
        EarlyVulkan.markFailed("test")

        assertTrue(Files.exists(polydiumDir.resolve("crash-recheck")))
    }

    @Test
    fun missedOldRecheckRunsAgain() {
        failAfterCrash(inputCrash)
        val old = polydiumDir.resolve("crash-recheck")
        Files.writeString(old, "")
        Files.setLastModifiedTime(old, beforeLaunch(Duration.ofSeconds(5)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun quickRelaunchAfterFailureIsNotAnotherLaunch() {
        failAfterCrash(inputCrash)
        rotatedLog("2026-11-12-3.log.gz", beforeLaunch(Duration.ofSeconds(20)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun thisLaunchsLogRotationIsNotAnotherLaunch() {
        failAfterCrash(inputCrash)
        rotatedLog("2026-11-12-3.log.gz", FileTime.from(launchedAt.plusSeconds(1)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
    }

    @Test
    fun renderCrashFromOlderBuildIsAFailedStart() {
        crashAfterUncleanStart(vulkanCrash)

        EarlyVulkan()

        assertFalse(EarlyVulkan.vulkanAtStartup())
        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun failureRecordedByThisBuildIsNotRechecked() {
        failAfterCrash(inputCrash)
        EarlyVulkan.markFailed("automatic Vulkan switch failed, started on OpenGL")
        EarlyVulkan.setGraphicsApi("default")

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    //? if >= 26.2 {
    @Test
    fun glDriverLoadCrashIsDetected() {
        Files.write(options, listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "startedCleanly:false"))
        Files.setLastModifiedTime(options, FileTime.from(Instant.now().minus(Duration.ofMinutes(1))))
        val nativeCrash = gameDir.resolve("crash-reports/hs_err_pid1.log")
        Files.createDirectories(nativeCrash.parent)
        Files.writeString(nativeCrash, "j  org.lwjgl.sdl.SDLVideo.nSDL_GL_LoadLibrary(J)Z+6\n")

        EarlyVulkan()
        assertTrue(EarlyVulkan.glLoadCrashed)

        Files.writeString(nativeCrash, "j  org.lwjgl.vulkan.VK10.vkCreateDevice\n")
        EarlyVulkan()
        assertFalse(EarlyVulkan.glLoadCrashed)
    }
    //?}

    @Test
    fun renderStartupCrashKeepsFailure() {
        failAfterCrash(vulkanCrash)

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun nativeCrashAfterReportKeepsFailure() {
        val report = failAfterCrash(inputCrash)
        val nativeCrash = report.resolveSibling("hs_err_pid1.log")
        Files.writeString(nativeCrash, "")
        Files.setLastModifiedTime(nativeCrash, beforeLaunch(Duration.ofMinutes(2)))

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun crashAfterFailureIsNotRechecked() {
        val report = failAfterCrash(inputCrash)
        Files.setLastModifiedTime(report, FileTime.from(Instant.now().plus(Duration.ofMinutes(1))))

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun backendPickedDuringSodiumRetryIsKept() {
        legacyFailure()
        EarlyVulkan.retrySodiumBlockedFailure()

        EarlyVulkan.setGraphicsApi("opengl")
        EarlyVulkan.setGraphicsApi("default")
        EarlyVulkan()

        assertEquals("checked", Files.readString(marker))
        assertEquals("default", EarlyVulkan.graphicsApi())
        //? if >= 26.2
        assertFalse(Files.exists(options))
    }
    //?}

    @Test
    fun freshInstallKeepsExplicitBackend() {
        EarlyVulkan.setGraphicsApi("opengl")

        EarlyVulkan()

        assertEquals("opengl", EarlyVulkan.graphicsApi())
        assertFalse(Files.exists(marker))
        //? if >= 26.2 {
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
        //?} else {
        /*assertFalse(Files.exists(options))
        *///?}
    }

    @Test
    fun existingOptionsKeepExplicitBackend() {
        Files.writeString(options, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\npreferredGraphicsBackend:\"opengl\"\n")

        EarlyVulkan()

        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
        //? if >= 26.2
        assertFalse(Files.exists(marker))
    }

    @Test
    fun uncleanStartIsNotSwitched() {
        Files.writeString(options, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nstartedCleanly:false\n")

        EarlyVulkan()

        assertFalse(Files.exists(marker))
    }

    //? if >= 26.2 {
    private fun writePack(file: Path, vararg lines: String) {
        Files.createDirectories(file.parent)
        Files.write(file, lines.toList())
    }

    private fun assertSwitchedFrom(vararg pack: String) {
        assertEquals("", Files.readString(marker))
        assertEquals(pack.toList() + "preferredGraphicsBackend:\"vulkan\"", Files.readAllLines(options))
    }

    @Test
    fun switchCarriesYosbrOptions() {
        writePack(yosbrOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")

        EarlyVulkan()

        assertSwitchedFrom("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")
    }

    @Test
    fun switchCarriesConfigManagerOptions() {
        writePack(configManagerOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")

        EarlyVulkan()

        assertSwitchedFrom("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")
    }

    @Test
    fun switchCarriesDefaultOptionsWithoutKeys() {
        writePack(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")

        EarlyVulkan()

        assertSwitchedFrom("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0")
    }

    @Test
    fun switchCarriesDefaultOptionsExtraFileWhole() {
        writePack(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:0.5")
        writePack(extraOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")

        EarlyVulkan()

        assertSwitchedFrom("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "key_key.jump:key.keyboard.j")
    }

    @Test
    fun packBackendReachesFileBeforeDefaultOptionsCopies() {
        val pack = arrayOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"vulkan\"")
        writePack(defaultOptions, *pack)

        EarlyVulkan()

        assertFalse(Files.exists(marker))
        assertEquals(pack.toList(), Files.readAllLines(options))
    }

    @Test
    fun savedBackendBeatsPackBackendWhenOptionsMissing() {
        writePack(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"vulkan\"")
        EarlyVulkan.setGraphicsApi("opengl")

        EarlyVulkan()

        assertFalse(Files.exists(marker))
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
    }

    @Test
    fun savedDefaultBeatsPackBackendWhenOptionsMissing() {
        writePack(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"vulkan\"")
        EarlyVulkan.setGraphicsApi("default")

        EarlyVulkan()

        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"default\""), Files.readAllLines(options))
    }

    @Test
    fun savedBackendBeatsOldPackBackendWhenOptionsMissing() {
        writePack(defaultOptions, "version:100", "preferredGraphicsBackend:\"vulkan\"")
        EarlyVulkan.setGraphicsApi("opengl")

        EarlyVulkan()

        assertEquals(listOf("version:100", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
    }

    @Test
    fun checkedMarkerStillRestoresSavedBackend() {
        EarlyVulkan.setGraphicsApi("opengl")
        Files.writeString(marker, "checked")

        EarlyVulkan()

        assertEquals("checked", Files.readString(marker))
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
    }

    @Test
    fun checkedMarkerLeavesDefaultChoiceAlone() {
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "checked")

        EarlyVulkan()

        assertFalse(Files.exists(options))
    }

    @Test
    fun checkedDefaultBeatsSharedVulkan() {
        val saved = listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"default\"")
        Files.write(options, saved)
        EarlyVulkan.setGraphicsApi("vulkan")
        Files.writeString(marker, "checked")

        EarlyVulkan()

        assertEquals("checked", Files.readString(marker))
        assertEquals(saved, Files.readAllLines(options))
    }

    @Test
    fun pendingSwitchFromOtherVersionBeatsSavedDefault() {
        Files.writeString(options, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nfov:1.0\npreferredGraphicsBackend:\"default\"\n")
        EarlyVulkan.setGraphicsApi("vulkan")
        Files.writeString(marker, "")

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"vulkan\""), Files.readAllLines(options))
    }

    @Test
    fun defaultPickedDuringPendingSwitchKeepsSavedDefault() {
        val saved = listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"default\"")
        Files.write(options, saved)
        EarlyVulkan.setGraphicsApi("default")
        Files.writeString(marker, "")

        EarlyVulkan()

        assertEquals(saved, Files.readAllLines(options))
    }

    @Test
    fun failedVulkanIsNotRestored() {
        Files.createDirectories(polydiumDir)
        Files.writeString(polydiumDir.resolve("graphics-api"), "vulkan")
        EarlyVulkan.markFailed("test")

        EarlyVulkan()

        assertFalse(Files.exists(options))
        assertTrue(EarlyVulkan.failed())
    }

    private fun failedIrisInstall(vararg irisProperties: String) {
        EarlyVulkan::class.java.getDeclaredField("irisLoaded").apply { isAccessible = true }.set(null, true)
        Files.createDirectories(irisConfig.parent)
        Files.write(irisConfig, irisProperties.toList())
        Files.writeString(options, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\npreferredGraphicsBackend:\"default\"\n")
        EarlyVulkan.setGraphicsApi("default")
        Files.createDirectories(polydiumDir)
        Files.writeString(marker, "failed")
    }

    @Test
    fun failedIrisInstallWithoutShadersRetriesOnce() {
        failedIrisInstall("enableShaders=true")

        EarlyVulkan()

        assertTrue(EarlyVulkan.switchPending())
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"vulkan\""), Files.readAllLines(options))

        EarlyVulkan.markFailed("test")
        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
    }

    @Test
    fun newIrisFailureIsNotRetried() {
        failedIrisInstall("enableShaders=true")
        EarlyVulkan.markFailed("test")

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"default\""), Files.readAllLines(options))
    }

    @Test
    fun failedIrisInstallWithShaderPackStaysOff() {
        failedIrisInstall("enableShaders=true", "shaderPack=BSL.zip")

        EarlyVulkan()

        assertTrue(EarlyVulkan.failed())
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"default\""), Files.readAllLines(options))
    }

    @Test
    fun disabledCleanupCreatesNoOptions() {
        System.setProperty("polydium.disable", "MixinKeepVulkanOptions")

        EarlyVulkan()

        assertFalse(Files.exists(options))
        assertFalse(Files.exists(marker))
    }

    @Test
    fun disabledCleanupStillSwitchesExistingOptions() {
        System.setProperty("polydium.disable", "MixinKeepVulkanOptions")
        Files.writeString(options, "version:${EarlyVulkan.BACKEND_FIX_VERSION}\nfov:1.0\n")

        EarlyVulkan()

        assertSwitchedFrom("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0")
    }

    @Test
    fun savedBackendBeatsPackOptions() {
        writePack(yosbrOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0")
        EarlyVulkan.setGraphicsApi("opengl")

        EarlyVulkan()

        assertFalse(Files.exists(marker))
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"opengl\""), Files.readAllLines(options))
    }

    @Test
    fun savedBackendBeatsOptionsYosbrAlreadyCopied() {
        val pack = arrayOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0")
        writePack(yosbrOptions, *pack)
        writePack(options, *pack)
        EarlyVulkan.setGraphicsApi("opengl")

        EarlyVulkan()

        assertFalse(Files.exists(marker))
        assertEquals(pack.toList() + "preferredGraphicsBackend:\"opengl\"", Files.readAllLines(options))
    }

    @Test
    fun retryWritesSavedVulkanOverPackOptions() {
        val pack = arrayOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"default\"")
        writePack(yosbrOptions, *pack)
        writePack(options, *pack)
        EarlyVulkan.setGraphicsApi("vulkan")
        Files.writeString(marker, "done")

        EarlyVulkan()

        assertEquals("checked", Files.readString(marker))
        assertEquals(listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0", "preferredGraphicsBackend:\"vulkan\""), Files.readAllLines(options))
    }

    @Test
    fun explicitBackendMatchingPackBeatsStaleApiFile() {
        val pack = arrayOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"opengl\"")
        writePack(yosbrOptions, *pack)
        writePack(options, *pack)
        EarlyVulkan.setGraphicsApi("vulkan")

        EarlyVulkan()

        assertEquals(pack.toList(), Files.readAllLines(options))
    }

    @Test
    fun createdOptionsAreRemovedForFirstLaunchChecks() {
        EarlyVulkan()
        assertTrue(Files.exists(options))

        EarlyVulkan.removeCreatedOptions()

        assertFalse(Files.exists(options))
    }

    @Test
    fun optionsThatExistedOrCarryPackAreNotRemoved() {
        Files.createFile(options)
        EarlyVulkan()
        EarlyVulkan.removeCreatedOptions()
        assertTrue(Files.exists(options))

        Files.delete(options)
        Files.delete(marker)
        writePack(yosbrOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0")
        EarlyVulkan()
        EarlyVulkan.removeCreatedOptions()
        assertTrue(Files.exists(options))
    }

    @Test
    fun optionsChangedDuringStartupAreNotRemoved() {
        EarlyVulkan()
        Files.writeString(options, "fov:1.0\n")

        EarlyVulkan.removeCreatedOptions()

        assertEquals(listOf("fov:1.0"), Files.readAllLines(options))
    }

    @Test
    fun failedWriteDoesNotClaimOptions() {
        val contents = listOf("version:${EarlyVulkan.BACKEND_FIX_VERSION}", "preferredGraphicsBackend:\"vulkan\"")
        Files.createDirectory(gameDir.resolve("options.txt.polydium"))

        EarlyVulkan()
        assertFalse(Files.exists(options))
        Files.write(options, contents)

        EarlyVulkan.removeCreatedOptions()

        assertEquals(contents, Files.readAllLines(options))
    }

    @Test
    fun copiedOptionsFollowModOrder() {
        val config = gameDir.resolve("config")
        writePack(yosbrOptions)
        writePack(configManagerOptions, "fov:1.0")

        assertEquals(configManagerOptions, EarlyVulkan.firstCopied(config, listOf("config_manager", "polydium", "yosbr"), "options.txt"))
        assertEquals(yosbrOptions, EarlyVulkan.firstCopied(config, listOf("polydium", "yosbr", "config_manager"), "options.txt"))
    }

    @Test
    fun emptyOptionsDoNotTakePackOptions() {
        Files.createFile(options)
        writePack(defaultOptions, "version:${EarlyVulkan.BACKEND_FIX_VERSION}", "fov:1.0")

        EarlyVulkan()

        assertSwitchedFrom("version:${EarlyVulkan.BACKEND_FIX_VERSION}")
    }

    @Test
    fun danglingOptionsLinkIsKept() {
        assumeFalse(System.getProperty("os.name").startsWith("Windows"))
        val target = gameDir.resolve("missing-dir/options.txt")
        Files.createSymbolicLink(options, target)

        EarlyVulkan()

        assertTrue(Files.isSymbolicLink(options))
        assertEquals(target, Files.readSymbolicLink(options))
        assertFalse(Files.exists(marker))
    }
    //?}
}

class EarlyVulkanGpuNameTest {

    @Test
    fun discreteNames() {
        for (name in listOf(
            "NVIDIA NVIDIA GeForce RTX 4070",
            "Advanced Micro Devices, Inc. AMD Radeon RX 6600 XT",
            "Intel Corporation Intel(R) Arc(TM) A770 Graphics",
            "Intel Corporation Intel(R) Arc(TM) B580 Graphics",
            "Intel Corporation Intel(R) Arc(TM) Pro B50 Graphics",
            "Advanced Micro Devices, Inc. Radeon (TM) RX 480 Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon(TM) RX 6600 XT",
            "Advanced Micro Devices, Inc. AMD RadeonT RX 6850M XT",
            "Advanced Micro Devices, Inc. AMD Radeon RX550 series",
            "Advanced Micro Devices, Inc. Radeon RX Vega",
            "Advanced Micro Devices, Inc. [AMD/ATI] Vega 10 XL/XT [Radeon RX Vega 56/64]",
            "Advanced Micro Devices, Inc. AMD Radeon VII",
            "Advanced Micro Devices, Inc. Radeon Vega Frontier Edition",
            "Advanced Micro Devices, Inc. AMD Radeon PRO W7900",
            "Advanced Micro Devices, Inc. Radeon (TM) Pro WX 9100",
            "Advanced Micro Devices, Inc. AMD Radeon AI PRO R9700",
            "Advanced Micro Devices, Inc. [AMD/ATI] Navi 48 [Radeon RX 9070/9070 XT/9070 GRE]",
            "Intel Corporation Intel(R) Arc(TM) Pro B70 Graphics",
            "Intel Corporation DG2 [Arc A770]",
        )) assertTrue(EarlyVulkan.isDiscreteName(name), name)

        for (name in listOf(
            "Intel Corporation Intel(R) UHD Graphics 630",
            "Intel Corporation Intel(R) Iris(R) Xe Graphics",
            "Intel Corporation Intel(R) Arc(TM) Graphics",
            "Intel Corporation Intel(R) Arc(TM) B390 Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon(TM) Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon(TM) RX Vega 11 Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon 780M Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon(TM) RX Vega 10 Graphics",
            "Advanced Micro Devices, Inc. Radeon RX Vega M GH Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon(TM) Pro Graphics",
            "Advanced Micro Devices, Inc. AMD Radeon(TM) 8060S Graphics",
            "Advanced Micro Devices, Inc. [AMD/ATI] Strix [Radeon 880M / 890M]",
            "Intel Corporation Intel(R) Arc(TM) 140V GPU (16GB)",
            "Intel Corporation Intel(R) Arc(TM) Pro 140T GPU (16GB)",
            "Intel Corporation Intel(R) Arc(TM) B390 GPU",
            "Intel Corporation Panther Lake [Arc Pro B390]",
            "Apple Apple M4 Pro",
        )) assertFalse(EarlyVulkan.isDiscreteName(name), name)
    }
}
