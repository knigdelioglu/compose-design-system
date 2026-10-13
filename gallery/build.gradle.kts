plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.knigdelioglu.arc.compose.gallery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.knigdelioglu.arc.compose.gallery"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures { compose = true }
}

dependencies {
    if (providers.gradleProperty("composeSmokePublished").orNull == "true") {
        val libraryVersion = providers.gradleProperty("composeLibraryVersion").orElse("0.1.0-SNAPSHOT").get()
        implementation("com.knigdelioglu:compose-design-system:$libraryVersion")
    } else {
        implementation(project(":design-system"))
    }
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
}
