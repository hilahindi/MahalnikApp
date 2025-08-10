plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Enables Firebase support and reads google-services.json
    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.example.mahalapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mahalapp"
        minSdk = 26
        targetSdk = 35
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //calender
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation ("com.prolificinteractive:material-calendarview:1.4.3")

    //contact
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //implementation (platform("com.google.firebase:firebase-bom:33.1.2")
    implementation("com.google.firebase:firebase-storage")


    // Firebase Authentication – enables user sign-in/sign-up
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Firestore – cloud-based NoSQL database for storing user profiles and data
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}


