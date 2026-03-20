rootProject.name = "morse-code-kmp"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared:morse-core")
include(":shared:practice")
include(":shared:communication")
include(":androidApp")
include(":webApp")
