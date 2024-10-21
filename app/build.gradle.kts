plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.skysphere.skysphere"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skysphere.skysphere"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.play.services.location)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation(libs.firebase.database)
    implementation(libs.androidx.junit.ktx)
    implementation ("com.firebaseui:firebase-ui-database:8.0.0")
    implementation(libs.androidx.junit.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.mpandroidchart)
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.androidx.work.runtime.ktx)
    // JSON Serialization dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation ("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

    implementation(libs.androidx.swiperefreshlayout)

    androidTestImplementation("junit:junit:4.13.2'")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    debugImplementation("androidx.fragment:fragment-testing:1.4.1")
    androidTestImplementation("org.mockito:mockito-core:3.12.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.3.6")

    testImplementation ("org.mockito:mockito-core:3.12.4")
    testImplementation ("org.mockito:mockito-inline:3.11.2")
    testImplementation ("androidx.test:core:1.5.0")
    testImplementation ("androidx.test.ext:junit:1.1.5")
    testImplementation ("androidx.fragment:fragment-testing:1.5.5")
    testImplementation ("androidx.test:runner:1.5.2")
    testImplementation ("org.robolectric:robolectric:4.9")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:3.2.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")




    // Coroutines dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    // Hilt Dependencies
    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    // Room Dependencies
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Lottie
    implementation ("com.airbnb.android:lottie:4.2.2")
}

kapt {
    correctErrorTypes = true
}