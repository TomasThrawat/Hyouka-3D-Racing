import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tomasthrawat.hyouka3dracing"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tomasthrawat.hyouka3dracing"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation("io.github.sceneview:sceneview:4.37.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
}

val assetUrls = mapOf(
    "models/car.glb" to "https://cdn.3dassets.dev/assets/14838/v1/model.glb",
    "models/track_start.glb" to "https://cdn.3dassets.dev/assets/15190/v1/model.glb",
    "models/track_straight.glb" to "https://cdn.3dassets.dev/assets/15182/v1/model.glb",
    "models/track_corner90.glb" to "https://cdn.3dassets.dev/assets/15184/v1/model.glb",
    "models/track_corner45.glb" to "https://cdn.3dassets.dev/assets/15185/v1/model.glb",
    "models/track_banked.glb" to "https://cdn.3dassets.dev/assets/15189/v1/model.glb",
    "models/guardrail.glb" to "https://cdn.3dassets.dev/assets/15210/v1/model.glb",
    "models/tower.glb" to "https://cdn.3dassets.dev/assets/14900/v1/model.glb",
    "models/timing.glb" to "https://cdn.3dassets.dev/assets/15235/v1/model.glb"
)

val downloadGameAssets by tasks.registering {
    val outputDir = layout.projectDirectory.dir("src/main/assets")
    outputs.dir(outputDir)
    doLast {
        assetUrls.forEach { (relativePath, url) ->
            val target = outputDir.file(relativePath).asFile
            if (!target.exists() || target.length() == 0L) {
                target.parentFile.mkdirs()
                target.outputStream().use { output ->
                    uri(url).toURL().openStream().use { input -> input.copyTo(output) }
                }
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(downloadGameAssets)
}
