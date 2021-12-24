// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    extra["hiltVersion"] = "2.40.4"
    extra["composeVersion"] = "1.1.0-beta04"
    extra["accompanistVersion"] = "0.21.4-beta"

    val hiltVersion: String by extra

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath(kotlin("gradle-plugin", "1.6.0"))
        classpath("com.google.android.gms:strict-version-matcher-plugin:1.2.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}