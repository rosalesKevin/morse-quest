plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(21)

    androidTarget()

    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:morse-core"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "morse.shared.practice"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }
}
