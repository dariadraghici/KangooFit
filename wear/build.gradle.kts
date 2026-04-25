plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kangoofit"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.kangoofit"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.wear:wear:1.3.0")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.wearable)
}