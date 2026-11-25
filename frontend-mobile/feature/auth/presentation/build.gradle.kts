import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose.resources { packageOfResClass = "org.example.project.auth.presentation.generated.resources" }

kotlin {
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "org.example.project.auth.presentation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
        withHostTest {}
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
        commonMain.dependencies {
            implementation(project(":feature:auth:domain"))
            implementation(project(":core:ui"))
            implementation(libs.decompose)
            implementation(libs.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
        }
    }
}
