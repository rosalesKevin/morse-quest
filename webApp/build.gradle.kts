plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":shared:morse-core"))
            implementation(project(":shared:practice"))
            implementation(project(":shared:communication"))
            implementation(compose.runtime)
            implementation(compose.html.core)
        }
    }
}
