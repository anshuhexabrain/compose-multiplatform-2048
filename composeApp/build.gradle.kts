import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.xxfast.kstore.common)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.squareup.okio)
            implementation(libs.xxfast.kstore.file)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.harawata.appdirs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.squareup.okio)
            implementation(libs.xxfast.kstore.file)
            // JNA for Bright Data SDK integration (Windows native DLL)
            implementation("net.java.dev.jna:jna:5.14.0")
            implementation("net.java.dev.jna:jna-platform:5.14.0")
        }

        iosMain.dependencies {
            implementation(libs.xxfast.kstore.file)
        }

        wasmJsMain.dependencies {
            implementation(libs.xxfast.kstore.storage)
        }
    }
}

android {
    namespace = "com.alexjlockwood.twentyfortyeight"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.alexjlockwood.twentyfortyeight"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.alexjlockwood.twentyfortyeight.MainKt"

        // Ensure we use the toolchain JDK which has jpackage
        val javaHomePath = project.extensions.getByType<JavaToolchainService>()
            .launcherFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
            .get()
            .metadata
            .installationPath
            .asFile
            .absolutePath

        javaHome = javaHomePath

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "2048 Hexa Game - Merge, Match, Master the Puzzle"
            packageVersion = "1.0.0"

            // Display name and description
            description = "2048 Hexa Game - Merge, Match, Master the Puzzle"
            copyright = "© 2026 Hexabrain Systems. All rights reserved."
            vendor = "Hexabrain Systems"

            // Windows-specific configuration
            windows {
                // Include DLLs and config in the application package
                includeAllModules = true

                // Windows installer properties
                menuGroup = "Hexa Games"
                upgradeUuid = "6dcd5c1c-cb3f-428d-8a34-2f5abdd190cc"

                // Windows icon (PNG file - will be converted to ICO automatically)
                iconFile.set(project.file("src/desktopMain/resources/icons/app-icon.ico"))

                // Create desktop shortcut
                shortcut = true
                dirChooser = true
                menu = true
                perUserInstall = false
            }

            // Linux configuration
            linux {
                packageName = "2048-hexa-game"
                debMaintainer = "support@hexabrain.com"
                menuGroup = "Games"
                appCategory = "Game"
                iconFile.set(project.file("src/desktopMain/resources/icons/app-icon.png"))
            }

            // macOS configuration
            macOS {
                bundleID = "com.hexabrain.2048hexagame"
                appCategory = "public.app-category.games"
                iconFile.set(project.file("src/desktopMain/resources/icons/app-icon.png"))
            }
        }

        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}

