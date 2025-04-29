plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.kalashianed.memeory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kalashianed.memeory"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    
    // Firebase Authentication
    implementation(libs.firebase.auth)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("com.google.android.material:material:1.11.0")
    
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}