plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val signingStorePath = providers.environmentVariable("SKYTOWER_KEYSTORE_PATH").orNull
val signingStorePassword = providers.environmentVariable("SKYTOWER_KEYSTORE_PASSWORD").orNull
val signingKeyAlias = providers.environmentVariable("SKYTOWER_KEY_ALIAS").orNull
val signingKeyPassword = providers.environmentVariable("SKYTOWER_KEY_PASSWORD").orNull
val releaseStoreFile = signingStorePath?.takeIf { it.isNotBlank() }?.let(::file)
val hasReleaseSigning = releaseStoreFile?.exists() == true &&
    !signingStorePassword.isNullOrBlank() &&
    !signingKeyAlias.isNullOrBlank() &&
    !signingKeyPassword.isNullOrBlank()

android {
    namespace = "com.elxvro.skytower"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.elxvro.skytower"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.5.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = requireNotNull(releaseStoreFile)
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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
