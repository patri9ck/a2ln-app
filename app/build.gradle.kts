import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "dev.patri9ck.a2ln"
        minSdk = 27
        targetSdk = 31

        versionCode = 5
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    buildFeatures {
        viewBinding = true
    }

    val keystoreFile = rootProject.file("keystore.properties")

    if (keystoreFile.exists()) {
        val keystoreProperties = Properties()

        keystoreProperties.load(FileInputStream(keystoreFile))

        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")

                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.android.material:material:1.6.1")
    implementation("org.zeromq:jeromq:0.5.2")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.navigation:navigation-fragment:2.5.0")
    implementation("androidx.navigation:navigation-ui:2.5.0")
}
