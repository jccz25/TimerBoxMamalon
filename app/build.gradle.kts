plugins {
    alias(libs.plugins.android.application)
}

android {
    //namespace = "com.example.timerboxmamalon"
    namespace = "com.jccz25.timerboxmamalon"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.jccz25.timerboxmamalon"
        //applicationId = "com.example.timerboxmamalon"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    //buildTypes {
    //    release {
    //        optimization {
    //            enable = false
    //        }
    //    }
    //}
    buildTypes {
        release {
            isMinifyEnabled = true  // Esto ofusca el código y quita lo que no usas
            isShrinkResources = true // Borra imágenes/layouts sin usar
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Este se queda así, no le muevas
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}