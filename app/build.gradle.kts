import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yorkyang2333.claudwecho"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.yorkyang2333.claudwecho"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val apiBaseUrl = localProperties.getProperty("API_BASE_URL") 
            ?: System.getenv("API_BASE_URL")
            ?: "\"http://unconfigured.local/\""
        buildConfigField("String", "API_BASE_URL", apiBaseUrl)
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")

  implementation(libs.androidx.palette.ktx)
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material.icons.extended)
  
  // Wear Compose
  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.wear.compose.navigation)
  implementation("androidx.wear:wear-input:1.2.0-alpha02")
  implementation(libs.play.services.wearable)

  // Koin
  implementation(libs.koin.androidx.compose)

  // Network & Serialization
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.kotlinx.serialization)
  implementation(libs.okhttp.logging)
  implementation(libs.kotlinx.serialization.json)

  // Media3
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)
  implementation(libs.media3.datasource.okhttp)

  // Coil
  implementation(libs.coil.compose)

  // Pinyin
  implementation("com.belerweb:pinyin4j:2.5.1")

  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
