import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// 发布签名配置从 keystore.properties 读取（该文件已 gitignore，不入库）。
// 缺失时（如 CI 未提供）releaseSigning 为 null，release 回退默认签名，避免构建直接失败。
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.hytp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.hytp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 默认（release/生产）后端地址占位，联调用 debug 覆盖
        buildConfigField("String", "BASE_URL", "\"https://api.hytp.com/\"")
    }

    buildToolsVersion = "36.1.0"

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // 联调服务器（腾讯云 HTTP+IP，nginx 80 → api 入口）。本机模拟器测本地时改回 http://10.0.2.2:8788/
            buildConfigField("String", "BASE_URL", "\"http://124.220.15.182/\"")
        }
        release {
            // 有 keystore.properties 时用正式签名；缺失则回退默认（不阻断本地/CI 构建）
            signingConfig = signingConfigs.findByName("release") ?: signingConfig
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DI (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 网络
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi.kotlin)

    // 导航
    implementation(libs.androidx.navigation.compose)

    // 本地存储
    implementation(libs.androidx.datastore.preferences)

    // 协程
    implementation(libs.kotlinx.coroutines.android)

    // lifecycle + compose 集成（collectAsStateWithLifecycle / hiltViewModel）
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // 图片加载（Coil）
    implementation(libs.coil.compose)

    // 阿里云 OSS 直传（STS 临时凭证；未配置时回退服务器中转上传）
    implementation(libs.aliyun.oss.android)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}