/*
 * Copyright (C) 2022 Patrick Zwick and contributors
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
import java.util.*

plugins {
    id("com.android.application")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "dev.patri9ck.a2ln"
        minSdk = 27
        targetSdk = 32

        versionCode = 7
        versionName = "1.2.1"
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
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.android.material:material:1.6.1")
    implementation("org.zeromq:jeromq:0.5.2")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.navigation:navigation-fragment:2.5.1")
    implementation("androidx.navigation:navigation-ui:2.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}
