pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Swing Music"
// App Module
include(":app")

// Core Modules
include(":core")
include(":network")
include(":uicomponent")

// Feature Modules
include(":feature:folder")

include(":feature:artist")
include(":feature:player")
