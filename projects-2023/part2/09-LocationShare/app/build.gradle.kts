import java.util.Properties
import groovy.json.JsonOutput

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val localConfig = Properties().apply {
    val configFile = rootProject.file("secrets.properties")
    if (configFile.exists()) configFile.inputStream().use { load(it) }
}
fun clientSetting(name: String): String =
    System.getenv(name) ?: localConfig.getProperty(name, "")

android {
    namespace = "com.hocheol.locationshare"
    compileSdk = 34

    defaultConfig {
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", JsonOutput.toJson(clientSetting("KAKAO_NATIVE_APP_KEY")))
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = clientSetting("KAKAO_NATIVE_APP_KEY")

        applicationId = "com.hocheol.locationshare"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.kakao.sdk:v2-user:2.19.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
