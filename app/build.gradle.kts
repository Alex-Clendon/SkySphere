plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
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
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.mpandroidchart)
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.androidx.work.runtime.ktx)

//    testImplementation("androidx.test:core:1.4.0")
//    testImplementation("junit:junit:4.13.2")
//    testImplementation("androidx.arch.core:core-testing:2.1.0")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
//    testImplementation("com.google.truth:truth:1.1.3")
//    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")
//    testImplementation("io.mockk:mockk:1.10.5")
//    debugImplementation("androidx.compose.ui:ui-test-manifest:1.1.0-alpha04")
//
//    // AndroidX Test Core library
//    testImplementation("androidx.test:core:1.5.0")
//    androidTestImplementation("androidx.test:core:1.5.0")
//
//    // AndroidX Test Rules
//    testImplementation("androidx.test:rules:1.5.0")
//    androidTestImplementation("androidx.test:rules:1.5.0")
//
//    // AndroidX Test Runner
//    androidTestImplementation("androidx.test:runner:1.5.2")
//
//    // AndroidX Junit Extension
//    testImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//
//    // AndroidX Fragment Testing
//    debugImplementation("androidx.fragment:fragment-testing:1.6.1")
//
//    // JUnit
//    testImplementation("junit:junit:4.13.2")
//
//    // Mockito
//    testImplementation("org.mockito:mockito-core:5.3.1")
//    testImplementation("org.mockito:mockito-android:5.3.1")
//
//    // MockK (for Kotlin-friendly mocking)
//    testImplementation("io.mockk:mockk:1.13.5")
//    androidTestImplementation("io.mockk:mockk-android:1.13.5")
//
//    // Espresso (for UI testing)
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//    // Firebase Testing
//    testImplementation("com.google.firebase:firebase-database:20.2.2")
//    testImplementation("org.robolectric:robolectric:4.10.3")

    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:3.11.2")
    testImplementation ("org.mockito:mockito-inline:3.11.2")
    testImplementation ("androidx.test:core:1.5.0")
    testImplementation ("androidx.test.ext:junit:1.1.5")
    testImplementation ("androidx.fragment:fragment-testing:1.5.5")
    testImplementation ("androidx.test:runner:1.5.2")
    testImplementation ("org.robolectric:robolectric:4.9")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:3.2.0")

}