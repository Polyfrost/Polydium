package org.polyfrost.polydium.test

import net.fabricmc.loader.api.FabricLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.early.EarlyVulkan
import java.nio.file.Files

class EarlyVulkanTest {

    @Test
    fun onlyVulkanChoiceSupersedesFailure() {
        val dir = FabricLoader.getInstance().gameDir.resolve("polydium")
        val marker = dir.resolve("auto-vulkan")
        val api = dir.resolve("graphics-api")
        assumeFalse(Files.exists(marker) || Files.exists(api))
        try {
            EarlyVulkan.markFailed("test")

            EarlyVulkan.setGraphicsApi("default")
            assertTrue(EarlyVulkan.failed())
            EarlyVulkan.setGraphicsApi("opengl")
            assertTrue(EarlyVulkan.failed())

            EarlyVulkan.setGraphicsApi("vulkan")
            assertFalse(EarlyVulkan.failed())
            assertTrue(EarlyVulkan.switched())
            assertEquals("vulkan", EarlyVulkan.graphicsApi())
        } finally {
            Files.deleteIfExists(marker)
            Files.deleteIfExists(api)
            Files.deleteIfExists(dir)
        }
    }

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
