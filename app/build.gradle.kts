plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    id("kotlin-parcelize")
//    alias(libs.plugins.google.services)
//    alias(libs.plugins.firebase.crashlytics)
    id("stringfog")
}

apply(plugin = "stringfog")
configure<com.github.megatronking.stringfog.plugin.StringFogExtension> {
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    enable = true
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}


android {
    namespace = "com.swift.newvpn"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.swift.newvpn"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        setProperty("archivesBaseName", "${rootProject.name}-v${versionName}-${versionCode}")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("scape_vpn.jks")
            storePassword = "20251204"
            keyAlias = "scape"
            keyPassword = "20251204"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-rules-release.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-rules-release.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        aidl = true
    }
    packaging {
        jniLibs.keepDebugSymbols.add("**/*.class")
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
        dex {
            useLegacyPackaging = true
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(listOf("libs"))
        }
    }
    splits {
        abi {
            reset()
            isEnable = false
            isUniversalApk = false
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.okhttp)
    implementation(libs.lottie)
    implementation(libs.gson)

    implementation(libs.installreferrer)
    implementation(libs.play.services.ads)

    implementation(platform(libs.firebase.bom))
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.config)

    implementation(libs.stringfog.xor)
    //facebook
    implementation(libs.facebook.android.sdk)
    //mmkv
    implementation(libs.mmkv)
    //adapter
    implementation(libs.base.adapter.helper)
    implementation(libs.androidx.appcompat)
}