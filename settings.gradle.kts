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
rootProject.name = "android-toolkit"
include(":core-ui", ":money-core", ":ai-coach", ":data-import", ":notif-capture", ":onboarding-kit", ":reminder-kit", ":store-assets")
