plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "co.kcagroforestry.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "co.kcagroforestry.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.siyamed:android-shape-imageview:0.9.3")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.1")

// Play service Location
    implementation("com.google.android.gms:play-services-location:21.2.0")

// Signature pad
    implementation("com.kyanogen.signatureview:signature-view:1.2")

// Sweet Alert Dialog
    implementation("com.github.f0ris.sweetalert:library:1.5.1")

// Circle ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

// Carousel View
    implementation("com.synnapps:carouselview:0.1.5")

// Picasso & Glide for image
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation("com.github.bumptech.glide:compiler:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.2")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:android-maps-utils:2.2.6")


// Firebase Crashlytics
    implementation(platform("com.google.firebase:firebase-bom:30.4.1"))
   // implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging:23.4.1")

// Circular Progress Bar
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")


// Auto-Update from google play
    implementation("com.google.android.play:app-update-ktx:2.0.0")

    //water mark
    implementation("com.huangyz0918:androidwm:0.2.3")
    implementation("com.huangyz0918:androidwm-light:0.1.2")
    implementation("com.whiteelephant:monthandyearpicker:1.3.0")



}