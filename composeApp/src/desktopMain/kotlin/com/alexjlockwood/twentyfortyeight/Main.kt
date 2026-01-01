package com.alexjlockwood.twentyfortyeight

import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
    // Initialize Bright Data SDK (Windows only)
    val brightSdk = createBrightDataSdk()
    if (brightSdk.isSupported()) {
        println("Bright Data SDK is supported on this platform")
        brightSdk.initialize()

        // Set up callback to track user consent changes
        brightSdk.setChoiceChangeCallback { choice ->
            println("Bright Data: User consent changed to: $choice")
        }
    } else {
        println("Bright Data SDK is not supported on this platform (expected on non-Windows)")
    }

    val filesDir = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION, AUTHOR).toPath()
    with(FileSystem.SYSTEM) { if (!exists(filesDir)) createDirectories(filesDir) }
    val store = storeOf(file = filesDir.resolve(USER_DATA_FILE_NAME), default = UserData.EMPTY_USER_DATA)

    Window(
        onCloseRequest = {
            // Clean up Bright Data SDK on exit
            brightSdk.close()
            exitApplication()
        },
        title = "2048",
    ) {
        // Add menu bar for Bright Data settings (Windows only)
        if (brightSdk.isSupported()) {
            MenuBar {
                Menu("Help") {
                    Item(
                        "Support This App",
                        onClick = {
                            when (brightSdk.getConsentChoice()) {
                                com.alexjlockwood.twentyfortyeight.brightsdk.ConsentChoice.NONE -> {
                                    brightSdk.showConsentDialog()
                                }
                                else -> {
                                    brightSdk.showConsentDialog()
                                }
                            }
                        }
                    )
                    Separator()
                    Item(
                        "About Bright Data",
                        onClick = {
                            val currentChoice = brightSdk.getConsentChoice()
                            println("Current Bright Data status: $currentChoice")
                            println("Click 'Support This App' to view or change your settings")
                        }
                    )
                }
            }
        }

        App(repository = GameRepository(store))
    }
}
