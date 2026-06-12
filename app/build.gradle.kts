plugins {
    id("com.android.application")
}

fun getVersionCode() : Int {
    val process = Runtime.getRuntime().exec("git tag")

    process.waitFor()

    return process.inputStream.bufferedReader().readText().trim().split("\n").size
}

fun getVersionName() : String {
    val process = Runtime.getRuntime().exec("git describe --tags --abbrev=0")

    process.waitFor()

    return process.inputStream.bufferedReader().readText().trim()
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "dev.patri9ck.a2ln"
        minSdk = 27
        targetSdk = 31

        versionCode = getVersionCode()
        versionName = getVersionName()
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.android.material:material:1.4.0")
    implementation("org.zeromq:jeromq:0.5.2")
}
