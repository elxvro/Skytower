plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val signingStorePath = providers.environmentVariable("SKYTOWER_KEYSTORE_PATH").orNull
val signingStorePassword = providers.environmentVariable("SKYTOWER_KEYSTORE_PASSWORD").orNull
val signingKeyAlias = providers.environmentVariable("SKYTOWER_KEY_ALIAS").orNull
val signingKeyPassword = providers.environmentVariable("SKYTOWER_KEY_PASSWORD").orNull
val hasReleaseSigning = !signingStorePath.isNullOrBlank() &&
    !signingStorePassword.isNullOrBlank() &&
    !signingKeyAlias.isNullOrBlank() &&
    !signingKeyPassword.isNullOrBlank() &&
    file(signingStorePath!!).exists()

android {
    namespace = "com.elxvro.skytower"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.elxvro.skytower"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(signingStorePath!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
