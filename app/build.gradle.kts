plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
     id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fadebarber"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fadebarber"
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
     implementation(libs.androidx.ui)
     implementation(libs.androidx.ui.graphics)
     implementation(libs.androidx.ui.tooling.preview)
     implementation(libs.androidx.material3)
     implementation(libs.androidx.foundation)
     implementation(libs.firebase.auth)
     implementation(libs.androidx.runtime.livedata)
     implementation(libs.androidx.compose.animation.core.lint)
     testImplementation(libs.junit)
     androidTestImplementation(libs.androidx.junit)
     androidTestImplementation(libs.androidx.espresso.core)
     androidTestImplementation(platform(libs.androidx.compose.bom))
     androidTestImplementation(libs.androidx.ui.test.junit4)
     debugImplementation(libs.androidx.ui.tooling)
     debugImplementation(libs.androidx.ui.test.manifest)
     implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
     implementation("androidx.compose.ui:ui:1.6.0")
     implementation("androidx.compose.material3:material3:1.2.0")
     implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
     implementation("androidx.activity:activity-compose:1.8.2")
     implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
     implementation("com.google.firebase:firebase-auth")
     implementation("com.google.firebase:firebase-database")
     implementation("androidx.compose.material:material-icons-extended")


    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))


    // Firebase dependencies (without KTX - the main modules now include Kotlin extensions)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")

    // Coroutines support for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Hilt and Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //estatus bar dependecia
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
}

