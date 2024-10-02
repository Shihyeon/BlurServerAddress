plugins {
    id("java")
    id("idea")
    id("fabric-loom") version ("1.7.+")
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName.set("blurserveraddress-fabric")
}

sourceSets {
    main.get().apply {
        compileClasspath += project(":common").sourceSets.main.get().output
    }
}

repositories {
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")
    mappings(loom.layered {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modImplementation(module)
    }

    // Fabric API modules
    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-resource-loader-v0")
}

tasks.named("compileTestJava").configure {
    enabled = false
}

tasks.named("test").configure {
    enabled = false
}

loom {
//    if (project(":common").file("src/main/resources/blurserveraddress.accesswidener").exists())
//        accessWidenerPath.set(project(":common").file("src/main/resources/blurserveraddress.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin { defaultRefmapName.set("blurserveraddress.fabric.refmap.json") }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
    }

}

tasks {
    withType<JavaCompile> {
        source(project(":common").sourceSets.main.get().allSource)
    }

    processResources {
        from(project.project(":common").sourceSets.main.get().resources)

        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    jar {
        from(rootDir.resolve("LICENSE"))
    }
}