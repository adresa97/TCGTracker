import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keyStore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} else {
    keystoreProperties.setProperty("SIGN_KEY_PATH", System.getenv("SIGN_KEY_PATH"))
    keystoreProperties.setProperty("SIGN_KEY_PASSWORD", System.getenv("SIGN_KEY_PASSWORD"))
    keystoreProperties.setProperty("SIGN_KEY_ALIAS", System.getenv("SIGN_KEY_ALIAS"))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["SIGN_KEY_PATH"] as String)
            storePassword = keystoreProperties["SIGN_KEY_PASSWORD"] as String
            keyAlias = keystoreProperties["SIGN_KEY_ALIAS"] as String
            keyPassword = keystoreProperties["SIGN_KEY_PASSWORD"] as String
        }
    }
    namespace = "com.boogie_knight.tcgtracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.boogie_knight.tcgtracker"
        minSdk = 29
        targetSdk = 36
        versionCode = 8
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.compose.bom.v20250101)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.navigation3)
    implementation(libs.gson)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.compose.extended.colors)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.appcompat)

}