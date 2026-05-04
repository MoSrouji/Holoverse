// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
    alias(libs.plugins.google.firebase.firebase.perf) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    //id("kotlin-parcelize")
}

tasks.register("testClasses") {
    // This task is often expected by IDEs or external tools.
    // We can make it depend on the actual test compilation tasks in subprojects if needed.
}
