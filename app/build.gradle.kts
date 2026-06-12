plugins {
    id("com.android.application")
}

fun runCommand(command : String) : String {
    val process = Runtime.getRuntime().exec(command)

    process.waitFor()

    return process.inputStream.bufferedReader().readText().trim()
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "dev.patri9ck.a2ln"
        minSdk = 27
        targetSdk = 31

        versionCode = runCommand("git tag").split("\n").size
        versionName = runCommand("git describe --tags --abbrev=0")
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
