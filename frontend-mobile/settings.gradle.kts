rootProject.name = "KotlinProject"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
include(":core:network", ":core:ui")
include(":feature:catalog:domain", ":feature:catalog:data", ":feature:catalog:presentation")
include(":feature:auth:domain", ":feature:auth:data", ":feature:auth:presentation")
include(":feature:chat:presentation")
include(":feature:chat:domain", ":feature:chat:data")
include(":feature:profile:presentation")
include(":feature:settings:presentation")
