plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseSigningVariables = listOf(
    "DUOFOLD_RELEASE_STORE_FILE",
    "DUOFOLD_RELEASE_STORE_PASSWORD",
    "DUOFOLD_RELEASE_KEY_ALIAS",
    "DUOFOLD_RELEASE_KEY_PASSWORD",
)
val releaseSigningValues = releaseSigningVariables.associateWith { name ->
    System.getenv(name)?.takeIf { it.isNotBlank() }
}
val supplied = releaseSigningValues.filterValues { it != null }.keys
check(supplied.isEmpty() || supplied.size == releaseSigningVariables.size) {
    "Release signing needs all DUOFOLD_RELEASE_* vars. Missing: ${releaseSigningVariables.filterNot(supplied::contains)}"
}

val releaseStoreFile = releaseSigningValues["DUOFOLD_RELEASE_STORE_FILE"]?.let { path ->
    rootProject.file(path).canonicalFile.also { store ->
        val root = rootProject.projectDir.canonicalFile.toPath()
        check(!store.toPath().startsWith(root)) { "Keystore must live outside the repository." }
        check(store.isFile && store.canRead()) { "Keystore is not readable: $store" }
    }
}

/** Tag/CI can override: DUOFOLD_VERSION_NAME=0.7.0 DUOFOLD_VERSION_CODE=7000 */
fun envOr(name: String, default: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: default

fun envOrInt(name: String, default: Int): Int =
    System.getenv(name)?.toIntOrNull() ?: default

android {
    namespace = "com.duofold.launcher"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.duofold.launcher"
        minSdk = 31
        targetSdk = 37
        versionCode = envOrInt("DUOFOLD_VERSION_CODE", 6)
        versionName = envOr("DUOFOLD_VERSION_NAME", "0.6.0")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseSigningValues.getValue("DUOFOLD_RELEASE_STORE_PASSWORD")
                keyAlias = releaseSigningValues.getValue("DUOFOLD_RELEASE_KEY_ALIAS")
                keyPassword = releaseSigningValues.getValue("DUOFOLD_RELEASE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseStoreFile != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation("junit:junit:4.13.2")
}
