/*
 * Android 2 Linux Notifications - A way to display Android phone notifications on Linux
 * Copyright (C) 2023  patri9ck and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "dev.patri9ck.a2ln"

    compileSdk = 34

    defaultConfig {
        applicationId = "dev.patri9ck.a2ln"
        minSdk = 27
        targetSdk = 34

        versionCode = 19
        versionName = "1.4.0"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.zeromq:jeromq:0.5.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("net.jodah:expiringmap:0.5.11")
}
