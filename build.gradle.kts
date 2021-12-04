// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    extra["composeVersion"] = "1.1.0-beta04"
    extra["accompanistVersion"] = "0.21.4-beta"
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath(kotlin("gradle-plugin", "1.6.0"))
        classpath("com.google.android.gms:strict-version-matcher-plugin:1.2.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}