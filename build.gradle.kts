plugins {
    id("com.android.application")
}

android {
    namespace = "id.safina.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "id.safina.app"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
