plugins {
    id("multiloader-platform")

    id("fabric-loom") version ("1.11.+")
}

base {
    archivesName.set("blurserveraddress-fabric")
}

val configurationCommonModJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = true
}
val configurationCommonModResources: Configuration = configurations.create("commonResources") {
    isCanBeResolved = true
}

repositories {
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))

    configurationCommonModResources(project(path = ":common", configuration = "commonMainResources"))

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, BuildConfig.FABRIC_API_VERSION)
        modImplementation(module)
    }

    // Fabric API modules
    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-resource-loader-v0")
}

sourceSets.apply {
    main {
        compileClasspath += configurationCommonModJava
        runtimeClasspath += configurationCommonModJava
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")
    mappings(loom.layered {
        officialMojangMappings()

        if (BuildConfig.PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${BuildConfig.MINECRAFT_VERSION}:${BuildConfig.PARCHMENT_VERSION}@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
}

loom {
    mixin {
        useLegacyMixinAp = false
    }

    runs {
        named("client") {
            client()
            configName = "Fabric/Client"
            appendProjectPathToConfigName = false
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    jar {
        from(configurationCommonModJava)
    }

    remapJar {
        destinationDirectory.set(file(rootProject.layout.buildDirectory).resolve("mods"))
    }

    processResources {
        from(configurationCommonModResources)
    }
}