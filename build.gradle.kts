// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath(kotlin("gradle-plugin", "1.5.31"))
//        classpath ("com.google.dagger:hilt-android-gradle-plugin:$dagger_version")
//        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
        classpath("com.google.android.gms:strict-version-matcher-plugin:1.2.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}