plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kotlakiran.apptemplate"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kotlakiran.apptemplate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")

    // Toolkit modules. Locally these resolve via includeBuild("..") in
    // settings.gradle.kts; for a standalone app use the Jitpack coordinates.
    implementation("com.kotlakiran.toolkit:core-ui:0.1.0")
    implementation("com.kotlakiran.toolkit:ai-coach:0.1.0")
    implementation("com.kotlakiran.toolkit:money-core:0.1.0")
}
