package org.polyfrost.polydium.test

import net.fabricmc.loader.api.FabricLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.early.EarlyVulkan
import java.nio.file.Files

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
        EarlyVulkan::class.java.getDeclaredField("vulkanAtStartup").apply { isAccessible = true }.set(null, null)
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
    private fun writePack(file: java.nio.file.Path, vararg lines: String) {
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
    fun failedVulkanIsNotRestored() {
        Files.createDirectories(polydiumDir)
        Files.writeString(polydiumDir.resolve("graphics-api"), "vulkan")
        EarlyVulkan.markFailed("test")

        EarlyVulkan()

        assertFalse(Files.exists(options))
        assertTrue(EarlyVulkan.failed())
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
