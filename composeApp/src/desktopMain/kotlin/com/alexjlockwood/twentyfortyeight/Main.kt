package com.alexjlockwood.twentyfortyeight

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.alexjlockwood.twentyfortyeight.analytics.createAppAnalytics
import com.alexjlockwood.twentyfortyeight.brightsdk.createBrightDataSdk
import com.alexjlockwood.twentyfortyeight.domain.UserData
import com.alexjlockwood.twentyfortyeight.repository.GameRepository
import com.alexjlockwood.twentyfortyeight.repository.USER_DATA_FILE_NAME
import io.github.xxfast.kstore.file.storeOf
import net.harawata.appdirs.AppDirsFactory
import okio.FileSystem
import okio.Path.Companion.toPath

private const val PACKAGE_NAME = "com.alexjlockwood.twentyfortyeight"
private const val VERSION = "1.0.0"
private const val AUTHOR = "alexjlockwood"

fun main() = application {
    val brightSdk = createBrightDataSdk()
    if (brightSdk.isSupported()) {
        println("Bright Data SDK is supported on this platform")
        brightSdk.initialize()
    } else {
        println("Bright Data SDK is not supported on this platform (expected on non-Windows)")
    }

    val filesDir = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION, AUTHOR).toPath()
    with(FileSystem.SYSTEM) { if (!exists(filesDir)) createDirectories(filesDir) }
    val store = storeOf(file = filesDir.resolve(USER_DATA_FILE_NAME), default = UserData.EMPTY_USER_DATA)
    val analytics = createAppAnalytics(brightSdk)

    Window(
        onCloseRequest = {
            brightSdk.close()
            exitApplication()
        },
        title = "2048 Hexa Game - Merge, Match, Master the Puzzle",
    ) {
        App(
            repository = GameRepository(store),
            brightDataSdk = brightSdk,
            analytics = analytics
        )
    }
}
