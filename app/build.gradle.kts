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

        versionCode = 6
        versionName = "1.2.0"
        compileSdkVersion = "android-32"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_9)
        targetCompatibility(JavaVersion.VERSION_1_9)
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
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.android.material:material:1.6.1")
    implementation("org.zeromq:jeromq:0.5.2")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.navigation:navigation-fragment:2.5.0")
    implementation("androidx.navigation:navigation-ui:2.5.0")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.0.0")
    implementation("androidx.camera:camera-camera2:1.2.0-alpha03")
    implementation("androidx.camera:camera-lifecycle:1.2.0-alpha03")
    implementation("androidx.camera:camera-view:1.2.0-alpha03")
}
