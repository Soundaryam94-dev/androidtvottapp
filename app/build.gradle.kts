// ─────────────────────────────────────────────────────────────────────
//  build.gradle.kts (:app)
// ─────────────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    kotlin("plugin.serialization") version "1.9.22"  // ✅ matches Kotlin version
}

android {
    namespace  = "com.example.myottapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myottapp"
        minSdk        = 21
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"

        buildConfigField("String", "TMDB_API_KEY",
            "\"b0c28038dc8ad42a92385ae86c986b78\"")
        buildConfigField("String", "SUPABASE_URL",
            "\"https://jngqzbroftbvuhxwfgrl.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",
            "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpuZ3F6YnJvZnRidnVoeHdmZ3JsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxNjU1MTYsImV4cCI6MjA5Mjc0MTUxNn0.BAiB5hkZC6e0ZmV48iRi7ChEDv_f66PlLLBBPIt2-Z0\"")
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    // ── Compose BOM ───────────────────────────────────────────────────
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ── Hilt ─────────────────────────────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    // ── Serialization ─────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ── Navigation ────────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // ── ViewModel + Lifecycle ─────────────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ── Retrofit + Gson (TMDB API) ────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Coil (image loading) ──────────────────────────────────────────
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ── ExoPlayer / Media3 ────────────────────────────────────────────
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-common:1.4.1")

    // ── Room (local cache) ────────────────────────────────────────────
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Coroutines ────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ── DataStore ─────────────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Core ──────────────────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.15.0")

    // ── Supabase ─────────────────────────────────────────────────────
    // ✅ Kotlin DSL syntax (not Groovy)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-android:2.3.7")
}