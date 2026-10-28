import java.util.Properties

plugins { id("com.android.application"); kotlin("android"); kotlin("plugin.compose"); kotlin("plugin.serialization") }
val signingFile = System.getenv("SUDOKU_SIGNING_FILE")?.let { file(it) }
val privateSigning = Properties().apply { if (signingFile?.isFile == true) signingFile.inputStream().use(::load) }
android {
    namespace = "com.playereden.sudokutogether"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.playereden.sudokutogether"
        minSdk = 26; targetSdk = 35; versionCode = 12; versionName = "0.8.0-dev5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    if (privateSigning.isNotEmpty()) signingConfigs.create("privateKey") {
        storeFile = file(privateSigning.getProperty("storeFile"))
        storePassword = privateSigning.getProperty("storePassword")
        keyAlias = privateSigning.getProperty("keyAlias")
        keyPassword = privateSigning.getProperty("keyPassword")
    }
    buildTypes {
        debug {
            // QA uses a separate install so updating a test APK never
            // threatens the existing signed app's offline records.
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
        }
        release {
            isMinifyEnabled = false
            if (privateSigning.isNotEmpty()) signingConfig = signingConfigs.getByName("privateKey")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
