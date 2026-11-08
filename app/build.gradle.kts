plugins { id("com.android.application") }
android {
    namespace = "com.example.m3u8termux"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.m3u8termux"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
