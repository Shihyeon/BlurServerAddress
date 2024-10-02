object Constants {
    const val JAVA_VERSION: Int = 17

    // https://fabricmc.net/develop/
    const val MINECRAFT_VERSION: String = "1.20.4"
    const val YARN_MAPPINGS: String = "1.20.4+build.1"
    const val FABRIC_LOADER_VERSION: String = "0.15.11"
    const val FABRIC_API_VERSION: String = "0.97.2+1.20.4";

    // https://semver.org/
    const val MOD_VERSION: String = "1.0.0"
}

plugins {
    id("fabric-loom").version("1.7.+")
    id("java")
    id("maven-publish")
}

base {
    archivesName = "blurserveraddress"

    group = "kr.shihyeon.blurserveraddress"
    version = createVersionString()
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${Constants.MINECRAFT_VERSION}")
    mappings("net.fabricmc:yarn:${Constants.YARN_MAPPINGS}:v2")
    modImplementation("net.fabricmc:fabric-loader:${Constants.FABRIC_LOADER_VERSION}")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, Constants.FABRIC_API_VERSION)
        modImplementation(module)
    }
    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-resource-loader-v0")
}

tasks {
    processResources {
        val propertiesMap = mapOf(
            "version" to project.version,
            "minecraft_version" to Constants.MINECRAFT_VERSION,
            "loader_version" to Constants.FABRIC_LOADER_VERSION,
        )

        inputs.properties(propertiesMap)

        filesMatching("fabric.mod.json") {
            expand(propertiesMap)
        }
    }

    jar {
        from("${rootProject.projectDir}/LICENSE")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = Constants.JAVA_VERSION
}

fun createVersionString(): String {
    val builder = StringBuilder()

    val isReleaseBuild = System.getProperty("build.release") != null
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (isReleaseBuild) {
        builder.append(Constants.MOD_VERSION)
    } else {
        builder.append(Constants.MOD_VERSION.substringBefore('-'))
        builder.append("-snapshot")
    }

    builder.append("+mc").append(Constants.MINECRAFT_VERSION)

    if (!isReleaseBuild) {
        if (buildId != null) {
            builder.append("-build.${buildId}")
        } else {
            builder.append("-local")
        }
    }

    return builder.toString()
}
