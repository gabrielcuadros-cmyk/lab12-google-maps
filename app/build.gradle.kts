plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.example.lab12_maps"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.lab12_maps"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

secrets {
    // Archivo con la API Key real (NO se sube a Git, está en .gitignore)
    propertiesFileName = "secrets.properties"

    // Valores por defecto para que el proyecto compile sin la clave real
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {
    val mapsComposeVersion = "4.4.1"

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")

    // Utilidades de Google Maps para Jetpack Compose
    implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")

    // Widgets de Google Maps Compose
    implementation("com.google.maps.android:maps-compose-widgets:$mapsComposeVersion")

    // Ubicación del usuario (FusedLocationProviderClient)
    implementation(libs.play.services.location)

    // Puntos adicionales: Places SDK for Android (buscador de lugares)
    implementation(libs.places)
    // Places trae vectordrawable-animated 1.0.0, que repite el namespace "androidx.vectordrawable"
    // y AGP 9 no lo permite. Se fuerza una versión más nueva con su propio namespace.
    implementation("androidx.vectordrawable:vectordrawable-animated:1.2.0")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}