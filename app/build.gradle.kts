import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.protobuf)
}

android {
    namespace = "com.github.sky130.suiteki.pro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.sky130.suiteki.pro"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEV"
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
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        // this["release"].java.srcDir(protobuf.generatedFilesBaseDir)
        this["debug"].java.srcDir(protobuf.generatedFilesBaseDir)
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


    implementation(libs.okhttp)
    implementation(libs.okio)



    implementation(libs.gson)
    implementation(libs.flexbox)
    debugImplementation(libs.ui.tooling)


    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation(libs.materialkolor)


    implementation(libs.material.icon)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.animation)
    implementation(libs.fastble)

    implementation(libs.apache.commons.lang3)

    implementation(libs.flexible.bottomsheet.material3)

    implementation(libs.protobuf.java)

    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)

    implementation(libs.circularprogressbar.compose)

    implementation(libs.permission.flow.compose)

    implementation(files("lib/Bouncycastle.jar"))
}



protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.0.0"
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                id("java") {}
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.apply{
        add("-Xlint:deprecation")
        add("-Xlint:none")
    }
    options.isWarnings = false
}

afterEvaluate {
    tasks.named("kspDebugKotlin") {
        dependsOn("generateDebugProto")
    }
    tasks.named("kspReleaseKotlin") {
        dependsOn("generateReleaseProto")
    }
    tasks.named("generateReleaseProto") {
        dependsOn("compileDebugJavaWithJavac")
    }   
    tasks.named("generateDebugLintReportModel") {
        dependsOn("generateReleaseProto")
    }
    tasks.named("lintAnalyzeDebug") {
        dependsOn("generateReleaseProto")
    }
}



