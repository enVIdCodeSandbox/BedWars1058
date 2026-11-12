// BedWars1058 - Gradle build (replaces the former Maven build, same modules and artifacts).
//
// Requirements: JDK 21 or newer to run Gradle. All sources are Kotlin. Each module targets the Java release it used with Maven
// (see `javaRelease` in the module scripts).
//
// NMS modules need the Spigot server artifacts (org.spigotmc:spigot:<version>-R0.1-SNAPSHOT) from the CodeMC "nms"
// repository, or from `mavenLocal()` after running Spigot BuildTools with `--remapped` where needed.

plugins {
    // The whole project is Kotlin; the plugin is applied to every module below.
    kotlin("jvm") version "2.2.21" apply false
}

allprojects {
    group = "com.andrei1058.bedwars"
    version = providers.gradleProperty("version").get()

    repositories {
        // Only the groups that really are installed locally (Spigot BuildTools output, the sidebar library, this
        // project's own artifacts). A half downloaded file left in ~/.m2 for any other group can no longer break the build.
        mavenLocal {
            content {
                includeGroup("org.spigotmc")
                includeGroupByRegex("com\\.andrei1058.*")
            }
        }
        mavenCentral()
        // Repositories that only serve their own groups are filtered, so a slow or down repository can not
        // break the resolution of unrelated dependencies.
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            content { includeGroup("org.spigotmc"); includeGroup("net.md-5"); includeGroup("org.bukkit") }
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            content { includeGroup("net.md-5") }
        }
        maven("https://repo.codemc.io/repository/nms/") {
            content { includeGroup("org.spigotmc") }
        }
        maven("https://repo.codemc.io/repository/maven-public/") {
            content {
                includeGroup("net.citizensnpcs"); includeGroup("org.bstats")
                // VaultAPI and the bungeecord-chat snapshot used by the 1.18 API are only served here.
                includeGroup("net.milkbowl.vault"); includeGroup("net.md-5")
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        // The repository moved from andrei1058.com to andrei1058.dev. Only the VIP features API still comes from there,
        // the sidebar library is built from source (see SidebarLib/).
        maven("https://repo.andrei1058.dev/releases/") {
            content { includeGroupByRegex("com\\.andrei1058.*") }
        }
        maven("https://repo.andrei1058.com/releases/") {
            content { includeGroupByRegex("com\\.andrei1058.*") }
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            content { includeGroup("me.clip") }
        }
        maven("https://simonsator.de/repo/") {
            content { includeGroup("de.simonsator") }
        }
        maven("https://maven.citizensnpcs.co/repo") {
            content { includeGroup("net.citizensnpcs") }
        }
        maven("https://repo.alessiodp.com/releases/") {
            content { includeGroup("com.alessiodp.parties") }
        }
        maven("https://repo.glaremasters.me/repository/concuncan/") {
            content { includeGroup("com.grinderwolf"); includeGroup("com.flowpowered") }
        }
        maven("https://repo.rapture.pw/repository/maven-snapshots/") {
            content { includeGroup("com.grinderwolf") }
        }
        // ProtocolLib is optional at runtime (used by the built-in invisibility feature when installed).
        maven("https://repo.dmulloy2.net/repository/public/") {
            content { includeGroup("com.comphenix.protocol") }
        }
        // PacketEvents is a plugin the built-in replay feature needs on the server (compile-only here).
        maven("https://repo.codemc.io/repository/maven-releases/") {
            content { includeGroup("com.github.retrooper") }
        }
        maven("https://repo.infernalsuite.com/repository/maven-snapshots/") {
            content { includeGroup("com.infernalsuite.aswm") }
        }
    }
}

// The sidebar library keeps its own coordinates, so addons can use it from `publishToMavenLocal` like before.
val sidebarLibraryVersion = "25.2.2-SNAPSHOT"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    if (name.startsWith("sidebar-")) {
        group = "com.andrei1058.spigot.sidebar"
        version = sidebarLibraryVersion
        apply(plugin = "maven-publish")
        extensions.configure<PublishingExtension> {
            publications.create<MavenPublication>("maven") { from(components["java"]) }
        }
    }

    // modules override this in their own build script
    extra["javaRelease"] = 11

    dependencies {
        "compileOnly"("net.md-5:bungeecord-chat:1.8-SNAPSHOT")
        "compileOnly"("commons-io:commons-io:2.13.0")
        "compileOnly"("org.jetbrains:annotations:24.0.1")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(provider { project.extra["javaRelease"] as Int })
    }

    tasks.withType<Javadoc>().configureEach { enabled = false }

    // Every module compiles Kotlin for the Java release it targeted when it was written in Java.
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        afterEvaluate {
            val release = project.extra["javaRelease"] as Int
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.toVersion(release)
                targetCompatibility = JavaVersion.toVersion(release)
            }
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(release.toString()))
                compilerOptions.freeCompilerArgs.addAll(
                    // interface methods with bodies stay real JVM default methods, like in the former Java sources
                    "-Xjvm-default=all",
                    // The sources were converted from Java, where passing null is not checked at method entry.
                    // Keeping these checks off preserves that behaviour for callers (addons) that pass null.
                    "-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions"
                )
            }
        }
    }
}
