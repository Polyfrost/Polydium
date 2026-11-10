package org.polyfrost.polydium.early;

//? if >= 26.2 {
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkPhysicalDeviceDriverProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceDynamicRenderingFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMultiDrawFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceMultiDrawPropertiesEXT;
import org.lwjgl.vulkan.VkPhysicalDevicePortabilitySubsetFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceSynchronization2Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Properties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.slf4j.LoggerFactory;
//? if >= 26.3 {
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLVulkan;
//?} else {
/*import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
*///?}

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.vulkan.KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR;
import static org.lwjgl.vulkan.KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK12.*;

public final class VulkanProbe {
    private static final String STARTED = "polydium-vulkan-probe-started";
    private static final String OK = "polydium-vulkan-probe-ok";
    private static final String INCOMPLETE = "polydium-vulkan-probe-incomplete";
    private static final String VALIDATION = "--vulkanValidation";
    private static final String VALIDATION_LAYER = "VK_LAYER_KHRONOS_validation";
    private static final String DEBUG_UTILS = "--debugUtils";
    public static final String CRASHED = "Vulkan crashed in a test process";
    public static final String TIMED_OUT = "Vulkan test process timed out";
    private static final long TIMEOUT_SECONDS = 20;

    private static CompletableFuture<String> result;
    private static List<String> earlyLibraries;

    private VulkanProbe() {
    }

    public static void start() {
        start(false);
    }

    private static synchronized void start(boolean late) {
        if (late && earlyLibraries != null) {
            List<String> libraries = libraries(true);
            boolean retry = INCOMPLETE.equals(result.join()) && libraries != null && !earlyLibraries.containsAll(libraries);
            earlyLibraries = null;
            if (retry)
                result = probe(libraries);
            return;
        }
        if (result != null)
            return;
        List<String> libraries = libraries(late);
        if (libraries == null) {
            if (late) {
                LoggerFactory.getLogger("polydium/vulkan").warn("LWJGL jars not found for the Vulkan test process, using vanilla's check");
                result = CompletableFuture.completedFuture(null);
            }
            return;
        }
        if (!late)
            earlyLibraries = libraries;
        result = probe(libraries);
    }

    private static CompletableFuture<String> probe(List<String> libraries) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return run(libraries);
            } catch (Throwable t) {
                return null;
            }
        }, task -> {
            Thread thread = new Thread(task, "Polydium Vulkan probe");
            thread.setDaemon(true);
            thread.start();
        });
    }

    public static String failure() {
        start(true);
        String failure = result.join();
        return INCOMPLETE.equals(failure) ? null : failure;
    }

    public static boolean timedOut() {
        return result != null && TIMED_OUT.equals(result.getNow(null));
    }

    private static List<String> libraries(boolean late) {
        List<ClassLoader> loaders = late ? List.of(ClassLoader.getSystemClassLoader(), VulkanProbe.class.getClassLoader())
                : List.of(ClassLoader.getSystemClassLoader());
        Set<String> classpath = new LinkedHashSet<>();
        //? if >= 26.3 {
        String windowing = "org.lwjgl.sdl.SDLInit";
        //?} else
        /*String windowing = "org.lwjgl.glfw.GLFW";*/
        for (String library : List.of("org.lwjgl.Version", "org.lwjgl.vulkan.VK", windowing)) {
            String jar = null;
            for (ClassLoader loader : loaders) {
                try {
                    jar = Path.of(Class.forName(library, false, loader).getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
                    break;
                } catch (Exception ignored) {
                }
            }
            if (jar == null)
                return null;
            classpath.add(jar);
        }
        for (ClassLoader loader : loaders) {
            try {
                Enumeration<URL> manifests = loader.getResources("META-INF/MANIFEST.MF");
                while (manifests.hasMoreElements()) {
                    try {
                        if (manifests.nextElement().openConnection() instanceof JarURLConnection jar) {
                            Path file = Path.of(jar.getJarFileURL().toURI());
                            if (file.getFileName().toString().startsWith("lwjgl"))
                                classpath.add(file.toString());
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return List.copyOf(classpath);
    }

    private static String run(List<String> libraries) throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("polydium-vulkan-probe");
        try {
            String name = VulkanProbe.class.getName();
            for (Class<?> type : List.of(VulkanProbe.class, VanillaFailure.class)) {
                String path = type.getName().replace('.', '/') + ".class";
                Path classFile = dir.resolve(path);
                Files.createDirectories(classFile.getParent());
                try (InputStream in = VulkanProbe.class.getResourceAsStream("/" + path)) {
                    if (in == null)
                        return null;
                    Files.copy(in, classFile);
                }
            }

            boolean javaw = ProcessHandle.current().info().command()
                    .map(command -> Path.of(command).getFileName().toString().equalsIgnoreCase("javaw.exe")).orElse(false);
            String java = Path.of(System.getProperty("java.home"), "bin", javaw ? "javaw" : "java").toString();
            List<String> command = new ArrayList<>(List.of(java, "--enable-native-access=ALL-UNNAMED", "-Xmx64m", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1",
                    "-XX:-CreateCoredumpOnCrash", "-XX:ErrorFile=" + dir.resolve("hs_err_pid%p.log")));
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac"))
                command.add("-XstartOnFirstThread");
            System.getProperties().stringPropertyNames().stream()
                    .filter(key -> key.startsWith("org.lwjgl.") || key.equals("java.library.path"))
                    .filter(key -> !key.equals("org.lwjgl.vulkan.explicitInit"))
                    .forEach(key -> command.add("-D" + key + "=" + System.getProperty(key)));
            command.add("-Dorg.lwjgl.vulkan.explicitInit=true");
            List<String> classpath = new ArrayList<>(List.of(dir.toString()));
            classpath.addAll(libraries);
            command.addAll(List.of("-cp", String.join(File.pathSeparator, classpath), name));
            if (List.of(FabricLoader.getInstance().getLaunchArguments(false)).contains(VALIDATION))
                command.add(VALIDATION);
            if (debugUtilsWanted())
                command.add(DEBUG_UTILS);

            Path log = dir.resolve("output.txt");
            Process process = new ProcessBuilder(command).redirectErrorStream(true)
                    .redirectOutput(log.toFile()).start();
            Thread killChild = new Thread(process::destroyForcibly);
            Runtime.getRuntime().addShutdownHook(killChild);
            boolean exited;
            try {
                process.getOutputStream().close();
                exited = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!exited)
                    process.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
            } finally {
                try {
                    Runtime.getRuntime().removeShutdownHook(killChild);
                } catch (IllegalStateException ignored) {
                }
            }
            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            if (output.contains(OK))
                return null;
            if (!exited) {
                LoggerFactory.getLogger("polydium/vulkan").warn("Vulkan test process hung, skipping Vulkan this launch:\n{}", output);
                return TIMED_OUT;
            }
            if (!output.contains(STARTED) || output.contains(INCOMPLETE)) {
                LoggerFactory.getLogger("polydium/vulkan").info("Vulkan test process did not finish, using vanilla's check:\n{}", output);
                return INCOMPLETE;
            }
            int exit = process.exitValue();
            boolean killed = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")
                    ? (exit & 0xC0000000) != 0xC0000000 : Set.of(129, 130, 137, 143).contains(exit);
            if (killed && !Files.exists(dir.resolve("hs_err_pid" + process.pid() + ".log"))) {
                LoggerFactory.getLogger("polydium/vulkan").info("Vulkan test process was killed, using vanilla's check:\n{}", output);
                return null;
            }
            return CRASHED;
        } finally {
            try (var files = Files.walk(dir)) {
                files.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(path -> path.toFile().delete());
            } catch (IOException ignored) {
            }
        }
    }

    private static boolean debugUtilsWanted() {
        if (List.of(FabricLoader.getInstance().getLaunchArguments(false)).contains("--renderDebugLabels")
                || "1".equals(System.getenv("ENABLE_VULKAN_RENDERDOC_CAPTURE")))
            return true;
        try {
            Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
            for (String line : Files.exists(options) ? Files.readAllLines(options) : EarlyVulkan.packOptions())
                if (line.startsWith("glDebugVerbosity:"))
                    return Integer.parseInt(line.substring("glDebugVerbosity:".length()).strip()) > 0;
        } catch (IOException | NumberFormatException ignored) {
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(STARTED);
        System.out.flush();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VK.create();
            IntBuffer count = stack.callocInt(1);
            boolean validation = false;
            check(vkEnumerateInstanceLayerProperties(count, null));
            try (VkLayerProperties.Buffer layers = VkLayerProperties.calloc(count.get(0))) {
                check(vkEnumerateInstanceLayerProperties(count, layers));
                if (List.of(args).contains(VALIDATION))
                    for (VkLayerProperties layer : layers)
                        validation |= layer.layerNameString().equals(VALIDATION_LAYER);
            }
            boolean portability = false;
            boolean debugUtils = false;
            check(vkEnumerateInstanceExtensionProperties((CharSequence) null, count, null));
            try (VkExtensionProperties.Buffer properties = VkExtensionProperties.calloc(count.get(0))) {
                check(vkEnumerateInstanceExtensionProperties((CharSequence) null, count, properties));
                for (VkExtensionProperties property : properties) {
                    portability |= property.extensionNameString().equals(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
                    debugUtils |= property.extensionNameString().equals(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
                }
            }

            List<String> surface = surfaceExtensions();
            portability &= System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac");
            debugUtils &= validation || List.of(args).contains(DEBUG_UTILS);
            probeInstance(stack, surface, portability, false, false);
            if (validation || debugUtils)
                probeInstance(stack, surface, portability, validation, debugUtils);
        } catch (VanillaFailure ignored) {
        } catch (Throwable t) {
            System.out.println(INCOMPLETE);
            t.printStackTrace(System.out);
            System.exit(0);
        }
        System.out.println(OK);
        System.exit(0);
    }

    private static void probeInstance(MemoryStack stack, List<String> surface, boolean portability, boolean validation, boolean debugUtils) {
        List<String> enabled = new ArrayList<>(surface);
        enabled.remove(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
        if (portability)
            enabled.add(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
        if (debugUtils)
            enabled.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        PointerBuffer extensions = null;
        if (!enabled.isEmpty()) {
            extensions = stack.mallocPointer(enabled.size());
            for (String extension : enabled)
                extensions.put(stack.UTF8(extension));
            extensions.flip();
        }
        VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc(stack).sType$Default()
                .flags(portability ? VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR : 0)
                .pApplicationInfo(VkApplicationInfo.calloc(stack).sType$Default().pApplicationName(stack.UTF8("Minecraft Java Edition"))
                        .pEngineName(stack.UTF8("MinecraftJE")).apiVersion(VK_API_VERSION_1_2))
                .ppEnabledLayerNames(validation ? stack.pointers(stack.ASCII(VALIDATION_LAYER)) : null)
                .ppEnabledExtensionNames(extensions);
        IntBuffer count = stack.mallocInt(1);
        PointerBuffer pInstance = stack.mallocPointer(1);
        if (vkCreateInstance(info, null, pInstance) != VK_SUCCESS)
            return;
        VkInstance instance = new VkInstance(pInstance.get(0), info);
        try {
            if (vkEnumeratePhysicalDevices(instance, count, null) != VK_SUCCESS)
                return;
            PointerBuffer devices = stack.mallocPointer(count.get(0));
            int status = vkEnumeratePhysicalDevices(instance, count, devices);
            if (status != VK_SUCCESS && status != VK_INCOMPLETE)
                return;
            int deviceCount = count.get(0);
            VkPhysicalDevice first = null;
            VkPhysicalDevice selected = null;
            for (int i = 0; i < deviceCount; i++) {
                if (devices.get(i) == 0L)
                    continue;
                VkPhysicalDevice device = new VkPhysicalDevice(devices.get(i), instance);
                if (first == null)
                    first = device;
                if (!suitable(device, stack, true))
                    continue;
                if (selected == null) {
                    selected = device;
                } else if (discrete(device, stack) && !discrete(selected, stack)) {
                    selected = device;
                    break;
                }
            }
            //? if < 26.3 {
            /*// With no suitable device, 26.2 builds the first one again for its error message
            if (selected == null && first != null)
                suitable(first, stack, false);
            *///?}
            if (selected != null) {
                Set<String> selectedExtensions = extensions(selected);
                //? if >= 26.3 {
                if (selectedExtensions.contains("VK_KHR_portability_subset"))
                    triangleFans(selected, stack);
                //?}
                if (selectedExtensions.contains("VK_EXT_multi_draw"))
                    vkGetPhysicalDeviceFeatures2(selected, VkPhysicalDeviceFeatures2.calloc(stack).sType$Default()
                            .pNext(VkPhysicalDeviceMultiDrawFeaturesEXT.calloc(stack).sType$Default()));
            }
        } finally {
            vkDestroyInstance(instance, null);
        }
    }

    private static final Set<Integer> PROBLEMATIC_INTEL_MOLTENVK = Set.of(338, 341, 343, 346, 354, 358, 362, 1026, 1030, 1034, 1035,
            1038, 1042, 1046, 1050, 1051, 1054, 1058, 1062, 1066, 1067, 1070, 2562, 2566, 2570, 2571, 2574, 2578, 2582, 2586, 2587,
            2590, 2594, 2598, 2602, 2603, 2606, 3362, 3366, 3370, 3371, 3374, 3888, 3889, 3890, 3891, 5638, 5650, 5654, 5662, 5666,
            5670, 5674, 5675, 8880, 8881, 8882, 8883);

    private static boolean suitable(VkPhysicalDevice device, MemoryStack stack, boolean skipProblematic) {
        //? if < 26.3 {
        /*// 26.2 drops Vulkan 1.0 devices before any other query on them
        if (apiVersion(device, stack) < VK_API_VERSION_1_1)
            return false;
        *///?}
        Set<String> extensions = extensions(device);
        VkPhysicalDeviceDriverProperties driver = VkPhysicalDeviceDriverProperties.calloc(stack).sType$Default();
        VkPhysicalDeviceProperties2 properties = VkPhysicalDeviceProperties2.calloc(stack).sType$Default()
                .pNext(driver)
                .pNext(VkPhysicalDeviceVulkan11Properties.calloc(stack).sType$Default());
        if (extensions.contains("VK_EXT_multi_draw"))
            properties.pNext(VkPhysicalDeviceMultiDrawPropertiesEXT.calloc(stack).sType$Default());
        vkGetPhysicalDeviceProperties2(device, properties);
        boolean graphicsQueue = false;
        IntBuffer n = stack.mallocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(device, n, null);
        try (VkQueueFamilyProperties.Buffer families = VkQueueFamilyProperties.calloc(n.get(0))) {
            vkGetPhysicalDeviceQueueFamilyProperties(device, n, families);
            vkGetPhysicalDeviceFeatures2(device, VkPhysicalDeviceFeatures2.calloc(stack).sType$Default());
            int graphicsCompute = VK_QUEUE_GRAPHICS_BIT | VK_QUEUE_COMPUTE_BIT;
            for (int family = 0; family < families.capacity() && !graphicsQueue; family++) {
                if ((families.get(family).queueFlags() & graphicsCompute) != graphicsCompute)
                    continue;
                //? if >= 26.3 {
                graphicsQueue = SDLVulkan.SDL_Vulkan_GetPresentationSupport(device.getInstance(), device, family);
                //?} else
                /*graphicsQueue = GLFWVulkan.glfwGetPhysicalDevicePresentationSupport(device.getInstance(), device, family);*/
            }
        }
        //? if >= 26.3 {
        if (apiVersion(device, stack) < VK_API_VERSION_1_1)
            return false;
        //?}
        if (skipProblematic && driver.driverID() == VK_DRIVER_ID_MOLTENVK && properties.properties().vendorID() == 0x8086
                && PROBLEMATIC_INTEL_MOLTENVK.contains(properties.properties().deviceID()))
            return false;
        boolean supported = requiredFeatures(device, stack);
        //? if >= 26.3 {
        if (extensions.contains("VK_KHR_portability_subset"))
            supported &= triangleFans(device, stack);
        //?}
        return supported && graphicsQueue && properties.properties().apiVersion() >= VK_API_VERSION_1_2
                && extensions.containsAll(List.of("VK_KHR_dynamic_rendering", "VK_KHR_push_descriptor", "VK_KHR_synchronization2",
                "VK_EXT_vertex_attribute_divisor", "VK_KHR_swapchain"));
    }

    private static boolean requiredFeatures(VkPhysicalDevice device, MemoryStack stack) {
        VkPhysicalDeviceVulkan11Features vk11 = VkPhysicalDeviceVulkan11Features.calloc(stack).sType$Default();
        VkPhysicalDeviceVulkan12Features vk12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
        VkPhysicalDeviceSynchronization2Features sync2 = VkPhysicalDeviceSynchronization2Features.calloc(stack).sType$Default();
        VkPhysicalDeviceDynamicRenderingFeatures dynamicRendering = VkPhysicalDeviceDynamicRenderingFeatures.calloc(stack).sType$Default();
        VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT divisor = VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT.calloc(stack).sType$Default();
        VkPhysicalDeviceFeatures2 features = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default()
                .pNext(vk11).pNext(vk12).pNext(sync2).pNext(dynamicRendering).pNext(divisor);
        vkGetPhysicalDeviceFeatures2(device, features);
        //? if >= 26.3 {
        boolean versionSpecific = features.features().drawIndirectFirstInstance();
        //?} else
        /*boolean versionSpecific = features.features().fillModeNonSolid();*/
        return versionSpecific && features.features().multiDrawIndirect() && features.features().samplerAnisotropy()
                && vk11.shaderDrawParameters() && vk12.timelineSemaphore() && vk12.hostQueryReset() && sync2.synchronization2()
                && dynamicRendering.dynamicRendering() && divisor.vertexAttributeInstanceRateDivisor();
    }

    //? if >= 26.3 {
    private static boolean triangleFans(VkPhysicalDevice device, MemoryStack stack) {
        VkPhysicalDevicePortabilitySubsetFeaturesKHR portability = VkPhysicalDevicePortabilitySubsetFeaturesKHR.calloc(stack).sType$Default();
        vkGetPhysicalDeviceFeatures2(device, VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(portability));
        return portability.triangleFans();
    }
    //?}

    private static int apiVersion(VkPhysicalDevice device, MemoryStack stack) {
        VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);
        vkGetPhysicalDeviceProperties(device, properties);
        return properties.apiVersion();
    }

    private static boolean discrete(VkPhysicalDevice device, MemoryStack stack) {
        VkPhysicalDeviceProperties2 properties = VkPhysicalDeviceProperties2.calloc(stack).sType$Default();
        vkGetPhysicalDeviceProperties2(device, properties);
        return properties.properties().deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
    }

    private static final class VanillaFailure extends RuntimeException {
        VanillaFailure() {
            super(null, null, false, false);
        }
    }

    private static void check(int result) {
        if (result < 0)
            throw new VanillaFailure();
    }

    private static Set<String> extensions(VkPhysicalDevice device) {
        Set<String> extensions = new HashSet<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer n = stack.callocInt(1);
            check(vkEnumerateDeviceExtensionProperties(device, (CharSequence) null, n, null));
            try (VkExtensionProperties.Buffer properties = VkExtensionProperties.calloc(n.get(0))) {
                check(vkEnumerateDeviceExtensionProperties(device, (CharSequence) null, n, properties));
                for (VkExtensionProperties property : properties)
                    extensions.add(property.extensionNameString());
            }
        }
        return extensions;
    }

    private static List<String> surfaceExtensions() {
        //? if >= 26.3 {
        if (!SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO) || !SDLVulkan.SDL_Vulkan_LoadLibrary(((SharedLibrary) VK.getFunctionProvider()).getPath()))
            throw new IllegalStateException(SDLError.SDL_GetError());
        PointerBuffer names = SDLVulkan.SDL_Vulkan_GetInstanceExtensions();
        //?} else {
        /*if (GLFW.glfwPlatformSupported(GLFW.GLFW_PLATFORM_WAYLAND) && GLFW.glfwPlatformSupported(GLFW.GLFW_PLATFORM_X11))
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        if (!GLFW.glfwInit() || !GLFWVulkan.glfwVulkanSupported())
            throw new IllegalStateException("GLFW Vulkan unavailable");
        PointerBuffer names = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        *///?}
        if (names == null)
            throw new IllegalStateException("No surface extensions");
        List<String> extensions = new ArrayList<>();
        while (names.hasRemaining())
            extensions.add(MemoryUtil.memUTF8(names.get()));
        return extensions;
    }
}
//?}
