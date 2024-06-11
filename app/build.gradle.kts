plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
    id("io.realm.kotlin")
    id("com.google.dagger.hilt.android")
}

val buildType = project.gradle.startParameter.taskNames.find { it.contains("assemble") }
    ?.removePrefix("assemble") // Extract the build type from the task name
    ?.lowercase()
    ?: "debug" // Default to debug if no assemble task is found

println("Build type: $buildType")

android {
    namespace = "com.halidodat.neuralnetworkvisualizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.halidodat.neuralnetworkvisualizer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // dagger hilt
    implementation(libs.dagger.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // realm db
    implementation(libs.realm.base)
    implementation(libs.realm.sync)

    // Uniffi
    // NOTE: Having @aar is important or it wont be able to find the shared libs
    implementation("net.java.dev.jna:jna:5.14.0@aar")
    implementation(libs.kotlinx.coroutines.core)
}

tasks.register("SetupCargoNdk") {
    doLast {
        val osName = System.getProperty("os.name")
        if (!osName.startsWith("Linux")) {
            logger.warn("This script is designed for Unix-based systems.")
        }
        exec {
            commandLine("cargo", "install", "cargo-ndk")
        }
        exec {
            commandLine("rustup", "target", "add",
                "aarch64-linux-android",
                "armv7-linux-androideabi",
                "i686-linux-android",
                "x86_64-linux-android"
            )
        }
    }
}

tasks.register("BuildRustLibrary") {
    dependsOn("SetupCargoNdk")

//    var buildCargoType = if (buildType == "debug")  { "--lib" } else { "--$buildType" }

    val projectDir = project.rootDir
    doLast {
        exec {
            workingDir = projectDir
            commandLine("cargo", "build")
        }
        exec {
            workingDir = projectDir
            commandLine("cargo", "ndk", "-o", "app/src/main/jniLibs",
                "--manifest-path", "./Cargo.toml",
                "-t", "armeabi-v7a",
                "-t", "arm64-v8a",
                "-t", "x86",
                "-t", "x86_64",
                "build", "--release"
            )
        }
        exec {
            workingDir = projectDir
            commandLine("cargo", "run", "--bin", "uniffi-bindgen", "generate",
                "--library", "./target/debug/libmobile.so",
                "--language", "kotlin",
                "--out-dir", "app/src/main/java/rust")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("BuildRustLibrary")
}