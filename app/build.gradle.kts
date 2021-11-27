plugins {
    id("com.android.application")
//    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.android.gms.strict-version-matcher-plugin")
    id("com.mikepenz.aboutlibraries.plugin") version "10.0.0-b01"
    kotlin("android")
//    id("kotlin-kapt")
//    id("dagger.hilt.android.plugin")
}

val composeVersion = "1.0.5"

android {
    compileSdk = 31

    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\yangl\\android.jks")
            storePassword = "123456"
            keyAlias = "release"
            keyPassword = "123456"
        }
    }
    defaultConfig {
        applicationId = "me.yangle.myphone"
        minSdk = 30
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs["release"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")
    implementation("androidx.emoji2:emoji2:1.0.0-rc01")
    implementation("com.google.accompanist:accompanist-permissions:0.20.2")
//    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation ("com.mikepenz:aboutlibraries-core:10.0.0-b01")
    implementation ("org.osmdroid:osmdroid-android:6.1.10")
    implementation("androidx.preference:preference-ktx:1.1.1")
//    implementation 'androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03'
//    implementation 'androidx.hilt:hilt-navigation-compose:1.0.0-alpha03'
//    implementation "com.google.dagger:hilt-android:$dagger_version"
//    kapt 'androidx.hilt:hilt-compiler:1.0.0-beta01'
//    kapt "com.google.dagger:hilt-android-compiler:$dagger_version"

    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
}