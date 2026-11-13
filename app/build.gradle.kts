import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

fun resolveFirebaseApiKey(): String {
    val fromProp = localProperties.getProperty("FIREBASE_API_KEY")?.trim()
    if (!fromProp.isNullOrBlank()) return fromProp

    val fromEnv = System.getenv("FIREBASE_API_KEY")?.trim()
    if (!fromEnv.isNullOrBlank()) return fromEnv

    val googleServicesJson = file("google-services.json")
    if (googleServicesJson.exists()) {
        try {
            val content = googleServicesJson.readText()
            val match = Regex(""""current_key":\s*"([^"]+)"""").find(content)
            if (match != null) {
                return match.groupValues[1]
            }
        } catch (_: Exception) {}
    }
    return ""
}

val firebaseApiKey: String = resolveFirebaseApiKey()

android {
    namespace = "com.sumanonline.hisabkhata"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sumanonline.hisabkhata"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)
    implementation(libs.coil.compose)
    implementation(libs.zxing.core)
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Automated enforcement rule: fail the build if any hardcoded secret or API key is detected
tasks.register("verifyNoHardcodedSecrets") {
    description = "Enforces zero-hardcoded-secrets policy across project source code and build files."
    group = "verification"

    doLast {
        val secretPatterns = listOf(
            Regex("""AIza[0-9A-Za-z-_]{35}"""),
            Regex("""(?i)(api[_-]?key|secret[_-]?key|client[_-]?secret)\s*[:=]\s*["'][A-Za-z0-9_\-]{16,}["']""")
        )

        val scannedFiles = fileTree(projectDir) {
            include("src/**/*.kt", "src/**/*.java", "*.gradle.kts")
            exclude("**/build/**", "google-services.json")
        }

        val violations = mutableListOf<String>()

        scannedFiles.forEach { file ->
            val lines = file.readLines()
            lines.forEachIndexed { index, line ->
                secretPatterns.forEach { pattern ->
                    if (pattern.containsMatchIn(line)) {
                        violations.add("${file.relativeTo(rootDir)}:${index + 1} -> Detected potential hardcoded secret/API key: ${line.trim()}")
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                """
                SECURITY VIOLATION: Hardcoded secret or API key detected!
                The project strictly enforces that secrets, credentials, and API keys must NOT be hardcoded.
                Found violations:
                ${violations.joinToString("\n")}

                Please resolve credentials dynamically via local.properties, environment variables, or gitignored files.
                """.trimIndent()
            )
        }
    }
}

tasks.named("preBuild") {
    dependsOn("verifyNoHardcodedSecrets")
}

