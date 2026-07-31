import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

val localProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.canRead()) {
    keystorePropertiesFile.inputStream().use { stream ->
        keystoreProperties.load(stream)
    }
}

android {
    namespace = "app.grapheneos.deskclock"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "app.grapheneos.deskclock"
        minSdk = 37
        targetSdk = 37
        versionCode = 1_00_00
        versionName = "1.0.0"
    }

    val storePath = keystoreProperties.getProperty("storeFile")
        ?: System.getenv("KEYSTORE_FILE")
        ?: "android-keystore.jks"
    val storePasswordVal = keystoreProperties.getProperty("storePassword")
        ?: System.getenv("RELEASE_KEYSTORE_PASSWORD")
    val keyAliasVal = keystoreProperties.getProperty("keyAlias")
        ?: System.getenv("RELEASE_KEY_ALIAS")
    val keyPasswordVal = keystoreProperties.getProperty("keyPassword")
        ?: System.getenv("RELEASE_KEY_PASSWORD")

    val keystoreFile = rootProject.file(storePath)
    val hasValidKeystore = keystoreFile.exists() && !storePasswordVal.isNullOrEmpty()

    signingConfigs {
        create("release") {
            if (hasValidKeystore) {
                storeFile = keystoreFile
                storePassword = storePasswordVal
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
                enableV4Signing = true
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
        }

        installation {
            enableBaselineProfile = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("DeskClock-${variant.name}.apk")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    autoCorrect = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.koin.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.material)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.room)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)

    ksp(libs.androidx.room.compiler)
    detektPlugins(libs.bundles.detekt)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.bundles.compose.debug)
}
