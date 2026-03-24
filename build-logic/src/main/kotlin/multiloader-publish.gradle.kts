plugins {
    id("me.modmuss50.mod-publish-plugin")
}

val projectProviders = providers

gradle.projectsEvaluated {
    // https://github.com/modmuss50/mod-publish-plugin
    publishMods {
        //dryRun = true

        val changelogEnv = projectProviders.environmentVariable("CHANGELOG_TEXT").orNull
        changelog = if (!changelogEnv.isNullOrEmpty()) {
            changelogEnv
        } else {
            projectProviders.fileContents(layout.projectDirectory.file("changelog.md")).asText.get()
        }

        val versionType = projectProviders.environmentVariable("VERSION_TYPE").orElse("release").get()
        type = when (versionType) {
            "alpha" -> ALPHA
            "beta" -> BETA
            else -> STABLE
        }
        val includeSnapshot = projectProviders.environmentVariable("INCLUDE_SNAPSHOTS").orElse("false").get().toBoolean()
        val publishEnabled = projectProviders.environmentVariable("PUBLISH_ENABLED").orElse("true").get().toBoolean()
        val fabricEnabled = projectProviders.environmentVariable("FABRIC_ARTIFACT").orElse("true").get().toBoolean()
        val neoforgeEnabled = projectProviders.environmentVariable("NEOFORGE_ARTIFACT").orElse("true").get().toBoolean()

        val cfOptions = curseforgeOptions {
            accessToken = projectProviders.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1329238"
            minecraftVersionRange {
                start = BuildConfig.MINECRAFT_VERSION_MIN
                end = "latest"
            }
            clientRequired = true
            javaVersions.add(JavaVersion.toVersion(BuildConfig.JAVA_VERSION))
        }

        val mrOptions = modrinthOptions {
            accessToken = projectProviders.environmentVariable("MODRINTH_TOKEN")
            projectId = "559boQnq"
            minecraftVersionRange {
                start = BuildConfig.MINECRAFT_VERSION_MIN
                end = "latest"
                includeSnapshots = includeSnapshot
            }
        }

        val minecraftVersion = BuildConfig.MINECRAFT_VERSION.substringBefore('-')

        // Fabric
        if (publishEnabled && fabricEnabled) {
            curseforge("curseforgeFabric") {
                from(cfOptions)
                file = project(":fabric").tasks.named<Jar>("jar").flatMap { it.archiveFile }
                modLoaders.add("fabric")
                modLoaders.add("quilt")
                version = "mc${minecraftVersion}-${BuildConfig.MOD_VERSION}-fabric"
                displayName = "Blur Server Address ${BuildConfig.MOD_VERSION} for Fabric ${BuildConfig.MINECRAFT_VERSION}"
            }

            modrinth("modrinthFabric") {
                from(mrOptions)
                file = project(":fabric").tasks.named<Jar>("jar").flatMap { it.archiveFile }
                modLoaders.add("fabric")
                modLoaders.add("quilt")
                version = "mc${minecraftVersion}-${BuildConfig.MOD_VERSION}-fabric"
                displayName = "Blur Server Address ${BuildConfig.MOD_VERSION} for Fabric ${BuildConfig.MINECRAFT_VERSION}"
            }
        }

        // NeoForge
        if (publishEnabled && neoforgeEnabled) {
            curseforge("curseforgeNeoforge") {
                from(cfOptions)
                file = project(":neoforge").tasks.named<Jar>("jar").flatMap { it.archiveFile }
                modLoaders.add("neoforge")
                version = "mc${minecraftVersion}-${BuildConfig.MOD_VERSION}-neoforge"
                displayName = "Blur Server Address ${BuildConfig.MOD_VERSION} for NeoForge ${BuildConfig.MINECRAFT_VERSION}"
            }

            modrinth("modrinthNeoforge") {
                from(mrOptions)
                file = project(":neoforge").tasks.named<Jar>("jar").flatMap { it.archiveFile }
                modLoaders.add("neoforge")
                version = "mc${minecraftVersion}-${BuildConfig.MOD_VERSION}-neoforge"
                displayName = "Blur Server Address ${BuildConfig.MOD_VERSION} for NeoForge ${BuildConfig.MINECRAFT_VERSION}"
            }
        }

        // GitHub Release
        if (fabricEnabled || neoforgeEnabled) {
            github {
                accessToken = projectProviders.environmentVariable("GITHUB_TOKEN")
                repository = "shihyeon/blur-server-address"
                commitish = "main"
                tagName = "${BuildConfig.MOD_VERSION}+mc${BuildConfig.MINECRAFT_VERSION}"
                version = "${BuildConfig.MOD_VERSION}+mc${BuildConfig.MINECRAFT_VERSION}"
                displayName = "Blur Server Address ${BuildConfig.MOD_VERSION} for Minecraft ${BuildConfig.MINECRAFT_VERSION}"

                if (fabricEnabled) {
                    file = project(":fabric").tasks.named<Jar>("jar").flatMap { it.archiveFile }
                    if (neoforgeEnabled) {
                        additionalFiles.from(project(":neoforge").tasks.named<Jar>("jar").flatMap { it.archiveFile })
                    }
                } else {
                    file = project(":neoforge").tasks.named<Jar>("jar").flatMap { it.archiveFile }
                }
            }
        }
    }
}