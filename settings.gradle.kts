rootProject.name = "blur-server-address"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        mavenCentral()
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("neoforge")