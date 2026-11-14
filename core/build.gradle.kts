import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.hilt)
}

// Developer-local overrides live in local.properties (git-ignored). See local.properties.example.
val localProps =
    Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
val apiBaseUrl = localProps.getProperty("api.base.url", "https://wb-api.roa-dev.digital/")
// true runs every screen on the in-memory sample data (no network), as the starter did.
val useMockData = localProps.getProperty("use.mock.data", "false").toBoolean()
// The web app's sign-up page; empty hides the "Create your account on the website" link (owner, 14 Nov 2026).
val signUpUrl = localProps.getProperty("sign.up.url", "")
// Keycloak realm for "Continue with Google" (public client webblock-ios, PKCE).
val authRealmUrl = localProps.getProperty("auth.realm.url", "https://wb-auth.roa-dev.digital/realms/webblock")
// Google sign-in with the v2 API (on since 14 Nov 2026: no new accounts from the app, rule b).
val googleSignIn = localProps.getProperty("google.sign.in", "true").toBoolean()

android {
    namespace = "com.kshrd.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("boolean", "USE_MOCK_DATA", "$useMockData")
        buildConfigField("String", "SIGN_UP_URL", "\"$signUpUrl\"")
        buildConfigField("String", "AUTH_REALM_URL", "\"$authRealmUrl\"")
        buildConfigField("boolean", "GOOGLE_SIGN_IN", "$googleSignIn")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Network stack: Retrofit + OkHttp logging. The v2 API reads JSON with kotlinx.serialization (`KotlinxConverterFactory`);
    // Gson stays for the starter's `RemoteDataSource`. Exposed as `api` so features can declare Retrofit interfaces.
    api(libs.retrofit)
    api(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
