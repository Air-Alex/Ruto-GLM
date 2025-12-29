plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.rikka.refine)
}

android {
    namespace = "com.rosan.ext"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
        lint.targetSdk = 36
    }

    buildFeatures.aidl = true
    buildFeatures.buildConfig = false
}

dependencies {
    compileOnly(project(":hidden-api"))

    compileOnly(libs.annotation)
    runtimeOnly(libs.annotation)

    implementation(libs.ktx.coroutines.core)
    implementation(libs.ktx.coroutines.android)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.lsposed.hiddenapibypass)

    annotationProcessor(libs.rikka.refine.annotation.processor)
    compileOnly(libs.rikka.refine.annotation)

    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)

    implementation(libs.iamr0s.androidAppProcess)
}