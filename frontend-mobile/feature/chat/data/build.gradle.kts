import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins { alias(libs.plugins.kotlinMultiplatform); alias(libs.plugins.androidMultiplatformLibrary); alias(libs.plugins.kotlinSerialization) }
kotlin.sourceSets.matching { it.name == "commonTest" }.configureEach {
    dependencies { implementation(libs.kotlin.test) }
}
kotlin.sourceSets.matching { it.name == "androidHostTest" }.configureEach {
    dependencies { implementation(libs.kotlin.testJunit) }
}
kotlin { iosArm64(); iosSimulatorArm64(); android { namespace = "org.example.project.chat.data"; compileSdk = libs.versions.android.compileSdk.get().toInt(); minSdk = libs.versions.android.minSdk.get().toInt(); withHostTest {}; compilerOptions { jvmTarget = JvmTarget.JVM_11 } }; sourceSets { commonMain.dependencies { implementation(project(":feature:chat:domain")); implementation(project(":feature:catalog:domain")); implementation(libs.coroutines.core); implementation(libs.datastore.preferences); implementation(libs.ktor.serialization.json) } } }
