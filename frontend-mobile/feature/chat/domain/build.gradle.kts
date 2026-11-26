import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins { alias(libs.plugins.kotlinMultiplatform); alias(libs.plugins.androidMultiplatformLibrary) }
kotlin { iosArm64(); iosSimulatorArm64(); android { namespace = "org.example.project.chat.domain"; compileSdk = libs.versions.android.compileSdk.get().toInt(); minSdk = libs.versions.android.minSdk.get().toInt(); withHostTest {}; compilerOptions { jvmTarget = JvmTarget.JVM_11 } }; sourceSets { commonMain.dependencies { implementation(libs.coroutines.core) } } }
