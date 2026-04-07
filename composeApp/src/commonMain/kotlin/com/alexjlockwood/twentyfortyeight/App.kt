package com.alexjlockwood.twentyfortyeight

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexjlockwood.twentyfortyeight.analytics.AppAnalytics
import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk
import com.alexjlockwood.twentyfortyeight.brightsdk.ConsentChoice
import com.alexjlockwood.twentyfortyeight.repository.GameRepository
import com.alexjlockwood.twentyfortyeight.ui.*
import com.alexjlockwood.twentyfortyeight.viewmodel.GameViewModel
import kotlinx.coroutines.launch

/**
 * Navigation states for the app flow
 */
sealed class AppScreen {
    object Landing : AppScreen()
    object Game : AppScreen()
}

@Composable
fun App(
    repository: GameRepository,
    brightDataSdk: BrightDataSdk,
    analytics: AppAnalytics
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Landing) }
    var showConsentRequiredDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isWaitingForConsent by remember { mutableStateOf(false) }
    var hasShownStartupConsent by remember { mutableStateOf(false) }
    var consentChoice by remember { mutableStateOf(brightDataSdk.getConsentChoice()) }
    var pendingDialogBrdEvent by remember { mutableStateOf(false) }
    var pendingDirectOptOutEvent by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun requestConsentDialog(waitForGameAccess: Boolean) {
        pendingDialogBrdEvent = true
        if (waitForGameAccess) {
            isWaitingForConsent = true
        }
        brightDataSdk.showConsentDialog()
    }

    LaunchedEffect(Unit) {
        val initialChoice = brightDataSdk.getConsentChoice()
        consentChoice = initialChoice
        analytics.trackAppStart(initialChoice == ConsentChoice.OPTED_IN)

        brightDataSdk.setChoiceChangeCallback { choice ->
            println("Bright Data: User consent changed to: $choice")
            consentChoice = choice

            if (pendingDialogBrdEvent && choice != ConsentChoice.NONE) {
                analytics.trackBrdState(choice == ConsentChoice.OPTED_IN)
                pendingDialogBrdEvent = false
            }

            if (pendingDirectOptOutEvent && choice == ConsentChoice.OPTED_OUT) {
                analytics.trackBrdState(false)
                pendingDirectOptOutEvent = false
            }

            if (isWaitingForConsent && choice == ConsentChoice.OPTED_IN) {
                currentScreen = AppScreen.Game
                isWaitingForConsent = false
            }
        }

        brightDataSdk.setDialogClosedCallback {
            if (isWaitingForConsent || pendingDialogBrdEvent) {
                coroutineScope.launch {
                    println("Bright Data: Dialog closed, polling for consent status...")

                    var currentChoice: ConsentChoice
                    var attempts = 0
                    val maxAttempts = 7
                    val maxNoneAttempts = 3
                    var noneAttempts = 0

                    while (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(1000)
                        currentChoice = brightDataSdk.getConsentChoice()
                        consentChoice = currentChoice
                        attempts++

                        println("Bright Data: Poll attempt $attempts/$maxAttempts - choice: $currentChoice")

                        when (currentChoice) {
                            ConsentChoice.OPTED_IN -> {
                                if (pendingDialogBrdEvent) {
                                    analytics.trackBrdState(true)
                                    pendingDialogBrdEvent = false
                                }
                                if (isWaitingForConsent) {
                                    println("Bright Data: Service fully activated! Navigating to game")
                                    currentScreen = AppScreen.Game
                                    isWaitingForConsent = false
                                }
                                return@launch
                            }
                            ConsentChoice.OPTED_OUT -> {
                                println("Bright Data: Status is OPTED_OUT (could be declining or installing...)")
                            }
                            ConsentChoice.NONE -> {
                                noneAttempts++
                                println("Bright Data: Poll saw NONE ($noneAttempts/$maxNoneAttempts), waiting for final consent state...")
                                if (noneAttempts >= maxNoneAttempts) {
                                    println("Bright Data: User closed without making a choice")
                                    pendingDialogBrdEvent = false
                                    if (isWaitingForConsent) {
                                        showConsentRequiredDialog = true
                                        isWaitingForConsent = false
                                    }
                                    return@launch
                                }
                            }
                        }
                    }

                    currentChoice = brightDataSdk.getConsentChoice()
                    consentChoice = currentChoice
                    println("Bright Data: Timeout after ${maxAttempts}s, final choice: $currentChoice")

                    if (currentChoice == ConsentChoice.NONE) {
                        pendingDialogBrdEvent = false
                    } else if (pendingDialogBrdEvent) {
                        analytics.trackBrdState(currentChoice == ConsentChoice.OPTED_IN)
                        pendingDialogBrdEvent = false
                    }

                    if (isWaitingForConsent) {
                        showConsentRequiredDialog = true
                        isWaitingForConsent = false
                    }
                }
            }
        }
    }

    LaunchedEffect(brightDataSdk, hasShownStartupConsent) {
        if (hasShownStartupConsent || !brightDataSdk.isSupported()) {
            return@LaunchedEffect
        }

        val currentChoice = brightDataSdk.getConsentChoice()
        consentChoice = currentChoice
        if (currentChoice == ConsentChoice.OPTED_IN) {
            hasShownStartupConsent = true
            return@LaunchedEffect
        }

        kotlinx.coroutines.delay(1500)
        hasShownStartupConsent = true
        println("Bright Data: First launch detected, showing consent dialog on app startup")
        requestConsentDialog(waitForGameAccess = false)
    }

    val gameViewModel = viewModel { GameViewModel(repository) }

    AppTheme {
        Surface {
            when (currentScreen) {
                AppScreen.Landing -> {
                    LandingScreen(
                        onPlayClicked = {
                            if (!brightDataSdk.isSupported()) {
                                println("Bright Data: SDK not supported, allowing play without consent")
                                currentScreen = AppScreen.Game
                                return@LandingScreen
                            }

                            val currentChoice = brightDataSdk.getConsentChoice()
                            consentChoice = currentChoice
                            println("Bright Data: Play clicked - current choice: $currentChoice")

                            if (currentChoice == ConsentChoice.OPTED_IN) {
                                println("Bright Data: Already opted in, navigating to game")
                                currentScreen = AppScreen.Game
                            } else {
                                println("Bright Data: Not opted in, showing consent dialog")
                                requestConsentDialog(waitForGameAccess = true)
                            }
                        },
                        onSettingsClicked = {
                            showSettingsDialog = true
                        }
                    )
                }
                AppScreen.Game -> {
                    GameUi(
                        gridTileMovements = gameViewModel.gridTileMovements,
                        currentScore = gameViewModel.currentScore,
                        bestScore = gameViewModel.bestScore,
                        isGameOver = gameViewModel.isGameOver,
                        onNewGameRequest = { gameViewModel.startNewGame() },
                        onSwipeListener = { direction -> gameViewModel.move(direction) },
                        onQuitToLanding = { currentScreen = AppScreen.Landing }
                    )
                }
            }
        }

        if (showConsentRequiredDialog) {
            ConsentRequiredDialog(
                onAcceptClicked = {
                    showConsentRequiredDialog = false
                    requestConsentDialog(waitForGameAccess = true)
                },
                onDismiss = {
                    showConsentRequiredDialog = false
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentChoice = consentChoice,
                onEnableRequested = {
                    requestConsentDialog(waitForGameAccess = false)
                },
                onDisableConfirmed = {
                    pendingDirectOptOutEvent = true
                    brightDataSdk.optOut()
                },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

