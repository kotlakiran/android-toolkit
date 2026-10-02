pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "app-template"

// Local development against the toolkit source. Gradle substitutes the
// com.kotlakiran.toolkit:* coordinates with the included build automatically.
// For a standalone app repo, delete this line, add Jitpack to the repositories
// above, and use com.github.kotlakiran.android-toolkit:<module>:<tag> instead.
includeBuild("..")

include(":app")
