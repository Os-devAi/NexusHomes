plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.nexusdev.nexushomes"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nexusdev.nexushomes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation - usa solo una versión consistente
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Elimina estas dependencias conflictivas:
    // implementation("androidx.navigation:navigation-ui-ktx:2.9.5") // CONFLICTO
    // implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0") // OBSOLETA

    // foundation ya está incluida en compose-bom
    // implementation ("androidx.compose.foundation:foundation")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0")) // Versión más estable
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // para google maps
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Coil - usa la versión actual
    implementation("io.coil-kt:coil-compose:2.6.0")

    // imagekit for storage images
    implementation("com.github.imagekit-developer.imagekit-android:imagekit-android:3.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.github.imagekit-developer.imagekit-android:imagekit-android:3.0.1")
    implementation("com.github.imagekit-developer.imagekit-android:imagekit-glide-extension:3.0.1")
    implementation("com.github.imagekit-developer.imagekit-android:imagekit-picasso-extension:3.0.1")
    implementation("com.github.imagekit-developer.imagekit-android:imagekit-coil-extension:3.0.1")
    implementation("com.github.imagekit-developer.imagekit-android:imagekit-fresco-extension:3.0.1")
    implementation("com.facebook.fresco:nativeimagetranscoder") {
        version {
            strictly("2.6.0")
        }
    }

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// Para el Logging (Debugging de peticiones)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
// Conversor para manejar respuestas no-JSON (String/Scalars)
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

// Para manejo de URIs locales y Streams
    implementation("androidx.core:core-ktx:1.12.0")

    // Corrutinas para integración con las tareas de Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}