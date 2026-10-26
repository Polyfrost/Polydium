@file:Suppress("UnstableApiUsage")

import dev.detekt.gradle.Detekt
import dev.kikugie.loomx.LoomCompatDependencyExtension
import dev.kikugie.loomx.LoomCompatProjectExtension
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.prod.ClientProductionRunTask
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    java
    kotlin("jvm")
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    id("net.ltgt.errorprone") version "5.1.1"
    `maven-publish`
}

val stonecutter = extensions.getByName("stonecutter") as StonecutterBuildExtension
val mcVersion = stonecutter.current.version

run {
    val (version, loader) = stonecutter.current.project.split("-", limit = 2)
    stonecutter.properties.tags(version, loader)
}

fun optionalProperty(name: String): String? = findProperty(name)?.toString()?.takeIf { it.isNotBlank() }

stonecutter constants {
    put("skyboxify", optionalProperty("deps.skyboxify") != null)
    put("xaerominimap", optionalProperty("deps.xaerominimap") != null)
}

val minecraftPredicate = property("mod.mc_compat") as String
val modId = property("mod.id") as String
val modVersion = property("mod.version") as String
val fabricLoaderVersion = property("deps.fabric_loader") as String
val fabricLanguageKotlinVersion = property("deps.fabric_language_kotlin") as String
val compatibleVersions: List<String> = stonecutter.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

group = property("mod.group") as String
version = "$modVersion+$mcVersion"
base.archivesName = modId

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    mavenLocal()
    mavenCentral()
    google()
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
    maven("https://jitpack.io") {
        content { includeGroupAndSubgroups("com.github") }
    }
    maven("https://maven.fabricmc.net/") {
        content { includeGroupAndSubgroups("net.fabricmc") }
    }
    maven("https://central.sonatype.com/repository/maven-snapshots") {
        content { includeGroup("net.kyori") }
    }
    strictMaven("https://maven.terraformersmc.com/releases/", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.bawnorton.com/releases", "Bawnorton", "com.github.bawnorton.mixinsquared")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "net.kyori" && requested.name == "adventure-platform-fabric" && requested.version == "7.0.0-SNAPSHOT") {
            useVersion("7.0.0")
        }
    }
}

val javaVersion = if (stonecutter.current.parsed >= "26.1") 25 else 21

configure<KotlinJvmExtension> {
    jvmToolchain(javaVersion)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

if (stonecutter.current.parsed < "26.3") {
    sourceSets.test { kotlin.exclude("**/TxTextFastPathTest.kt") }
}

val vulkanBackport = stonecutter.current.parsed >= "1.21.11" && stonecutter.current.parsed < "26.2"
if (!vulkanBackport) sourceSets.main {
    java.exclude(
        "org/polyfrost/polydium/vulkan/**",
        "org/polyfrost/polydium/mixin/client/perf/vulkan/backport/**",
        "org/polyfrost/polydium/mixin/client/perf/sodium/vulkan/**",
        "org/polyfrost/polydium/mixin/late/**",
        "org/polyfrost/oneconfig/internal/ui/services/**",
    )
    resources.exclude(
        "mixins.polydium.late.json",
        "assets/polydium/shaders/core/imgui.*",
        "assets/polydium/shaders/core/odin_*",
        "assets/polydium/shaders/include/odin_*",
        "assets/polydium/shaders/include/sodium/**",
        "assets/polydium/shaders/sodium/**",
    )
}

if (vulkanBackport && stonecutter.current.parsed < "26.1") sourceSets.main {
    resources.exclude("assets/polydium/shaders/core/odin_*", "assets/polydium/shaders/include/odin_*")
}

if (vulkanBackport) {
    val moltenVk = configurations.detachedConfiguration(*listOf("macos", "macos-arm64").map {
        dependencies.create("org.lwjgl:lwjgl-vulkan:${findProperty("deps.moltenvk") ?: property("deps.lwjgl")}:natives-$it")
    }.toTypedArray())
    val moltenVkResources = tasks.register<Sync>("moltenVkResources") {
        from({ moltenVk.map { zipTree(it) } }) { include("macos/**", "META-INF/macos/**") }
        into(layout.buildDirectory.dir("generated/moltenvk"))
    }
    sourceSets.main { resources.srcDir(moltenVkResources) }
}

val loomExt = extensions.getByName<LoomGradleExtensionAPI>("loom")

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    dependencies.extensions.getByType<LoomCompatDependencyExtension>().applyMojangMappings()

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("deps.kotlin")}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${property("deps.mixin_extras")}")
    "com.github.bawnorton.mixinsquared:mixinsquared-fabric:${property("deps.mixin_squared")}".let {
        annotationProcessor(it)
        implementation(include(it)!!)
    }

    modLocalRuntime("me.djtheredstoner:DevAuth-fabric:${property("deps.devauth")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    runtimeOnly("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")

    val oneconfigVersion = property("deps.oneconfig") as String
    modImplementation("org.polyfrost.oneconfig:$mcVersion-fabric:$oneconfigVersion")
    for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "notifications", "ui", "utils", "hud")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigVersion")
    }

    optionalProperty("deps.sodium")?.let {
        modCompileOnly("maven.modrinth:sodium:$it") { isTransitive = false }
        if (providers.gradleProperty("polydium.runtimeSodium").isPresent) {
            modLocalRuntime("maven.modrinth:sodium:$it") { isTransitive = false }
            project.configurations.getByName("testRuntimeClasspath").extendsFrom(project.configurations.getByName("localRuntime"))
        }
    }
    optionalProperty("deps.iris")?.let {
        modCompileOnly("maven.modrinth:iris:$it") { isTransitive = false }
    }
    optionalProperty("deps.skyboxify")?.let {
        modCompileOnly("maven.modrinth:skyboxify:$it") { isTransitive = false }
    }
    optionalProperty("deps.xaerominimap")?.let {
        modCompileOnly("maven.modrinth:xaeros-minimap:$it") { isTransitive = false }
    }

    if (vulkanBackport) {
        val lwjgl = property("deps.lwjgl") as String
        val platforms = listOf("windows", "windows-arm64", "linux", "macos", "macos-arm64")
        include(implementation("org.lwjgl:lwjgl-vulkan:$lwjgl")!!)
        for (module in listOf("vma", "shaderc", "spvc")) {
            include(implementation("org.lwjgl:lwjgl-$module:$lwjgl")!!)
            for (os in platforms) include(runtimeOnly("org.lwjgl:lwjgl-$module:$lwjgl:natives-$os")!!)
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${property("deps.junit")}")
    testImplementation("net.fabricmc:fabric-loader-junit:$fabricLoaderVersion")

    errorprone("com.google.errorprone:error_prone_core:${property("deps.errorprone")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableAllChecks = true
        error("UnnecessarilyFullyQualified")
    }
}

// the enabled Detekt rules need type resolution, which only the per-compilation tasks have
tasks.named<Detekt>("detekt") {
    enabled = false
    dependsOn(tasks.withType<Detekt>().matching { it.name != "detekt" && !it.name.endsWith("SourceSet") })
}

loomExt.mixin {
    defaultRefmapName.set("mixins.$modId.refmap.json")
}

val modOutputGroup = sourceSets.main.get().output
    .let { files(it.classesDirs, it.resourcesDir) }
    .joinToString(File.pathSeparator) { it.absolutePath }

loomExt.runs.configureEach {
    ideConfigGenerated(true)
    runDir("../../run")
    vmArg("-Dfabric.classPathGroups=$modOutputGroup")
    optionalProperty("polydium.quickPlay")?.let { programArgs("--quickPlaySingleplayer", it) }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("fabric.classPathGroups", modOutputGroup)
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<ProcessResources>().configureEach {
    val modName = project.property("mod.name") as String
    val oneconfigVersion = project.property("deps.oneconfig") as String
    inputs.property("modId", modId)
    inputs.property("modName", modName)
    inputs.property("modVersion", modVersion)
    inputs.property("minorMcVersion", minecraftPredicate)
    inputs.property("oneconfigVersion", oneconfigVersion)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to modId,
                "mod_name" to modName,
                "mod_version" to modVersion,
                "mod_description" to "Client performance optimizations for OneClient",
                "minor_mc_version" to minecraftPredicate,
                "oneconfig_version" to oneconfigVersion,
            ),
        )
    }
    filesMatching("mixins.*.json") {
        expand("id" to modId)
    }
}

tasks.register<ClientProductionRunTask>("prodClient") {
    optionalProperty("polydium.gameDir")?.let { runDir.set(file(it)) }
}

tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
    dependsOn("stonecutterGenerate")
}

val modrinthId = listOf("oneconfig.publish.modrinth", "publish.modrinth").firstNotNullOfOrNull(::optionalProperty)
val modrinthToken = listOf("oneconfig.publish.modrinth.token", "publish.modrinth.token", "modrinth.token")
    .firstNotNullOfOrNull(::optionalProperty)

val curseforgeId = listOf("oneconfig.publish.curseforge", "publish.curseforge").firstNotNullOfOrNull(::optionalProperty)
val curseforgeToken = listOf("oneconfig.publish.curseforge.token", "publish.curseforge.token", "curseforge.token")
    .firstNotNullOfOrNull(::optionalProperty)

val changelogs = rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

val validateChangelog = tasks.register("validateChangelog") {
    description = "Validates that the changelog is written for the current version."
    group = "publishing"
    doLast {
        if (!changelogs.contains(modVersion)) {
            throw GradleException("Changelog for version $modVersion not found.")
        }
    }
}

tasks.publishMods.configure {
    dependsOn(validateChangelog)
}
tasks.matching { it.name == "publishModrinth" }.configureEach {
    dependsOn(validateChangelog)
}

val modJar = extensions.getByType<LoomCompatProjectExtension>().modJar

publishMods {
    file = modJar.flatMap { it.archiveFile }

    displayName = modVersion
    version = "v$modVersion"
    changelog = changelogs
    type = STABLE

    modLoaders.add("fabric")

    dryRun = (modrinthId == null || modrinthToken == null) && (curseforgeId == null || curseforgeToken == null)

    if (modrinthId != null) {
        modrinth {
            projectId = modrinthId
            accessToken = modrinthToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcVersion) })

            environment = CLIENT_ONLY

            requires("oneconfig")
            requires("fabric-api")
            requires("fabric-language-kotlin")
        }
    }

    if (curseforgeId != null) {
        curseforge {
            projectId = curseforgeId
            accessToken = curseforgeToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcVersion) })

            client = true
            server = false

            requires("oneconfig")
            requires("fabric-api")
            requires("fabric-language-kotlin")
        }
    }
}

publishing {
    repositories {
        maven("https://repo.polyfrost.org/releases") {
            name = "releases"
            credentials {
                username = providers.gradleProperty("polyfrostRepoUsername").orNull
                password = providers.gradleProperty("polyfrostRepoToken").orNull
            }
            authentication { create<BasicAuthentication>("basic") }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
        }
    }
}
