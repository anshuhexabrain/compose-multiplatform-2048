package com.alexjlockwood.twentyfortyeight

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk
import com.alexjlockwood.twentyfortyeight.brightsdk.ConsentChoice
import com.alexjlockwood.twentyfortyeight.repository.GameRepository
import com.alexjlockwood.twentyfortyeight.ui.*
import com.alexjlockwood.twentyfortyeight.viewmodel.GameViewModel

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
    brightDataSdk: BrightDataSdk
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Landing) }
    var showConsentRequiredDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isWaitingForConsent by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Set up callbacks to track consent changes
    LaunchedEffect(Unit) {
        brightDataSdk.setChoiceChangeCallback { choice ->
            println("Bright Data: User consent changed to: $choice")
            if (isWaitingForConsent && choice == ConsentChoice.OPTED_IN) {
                // User accepted consent, navigate to game
                currentScreen = AppScreen.Game
                isWaitingForConsent = false
            } else if (isWaitingForConsent && choice == ConsentChoice.OPTED_OUT) {
                // User declined consent, show dialog
                showConsentRequiredDialog = true
                isWaitingForConsent = false
            }
        }

        brightDataSdk.setDialogClosedCallback {
            // If dialog was closed and user is still waiting
            if (isWaitingForConsent) {
                // Launch coroutine to poll for consent status
                coroutineScope.launch {
                    println("Bright Data: Dialog closed, polling for consent status...")

                    // Poll for up to 15 seconds, checking every second for OPTED_IN
                    var currentChoice: ConsentChoice
                    var attempts = 0
                    val maxAttempts = 15
                    var foundOptedIn = false

                    while (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(1000)
                        currentChoice = brightDataSdk.getConsentChoice()
                        attempts++

                        println("Bright Data: Poll attempt $attempts/$maxAttempts - choice: $currentChoice")

                        when (currentChoice) {
                            ConsentChoice.OPTED_IN -> {
                                // Service is fully active! This is the ONLY case we let them play
                                println("Bright Data: ✓ Service fully activated! Navigating to game")
                                currentScreen = AppScreen.Game
                                isWaitingForConsent = false
                                foundOptedIn = true
                                return@launch
                            }
                            ConsentChoice.OPTED_OUT -> {
                                // Could be: 1) User declined, or 2) User accepted but service still activating
                                // Keep polling to see if it becomes OPTED_IN
                                println("Bright Data: Status is OPTED_OUT (could be declining or installing...)")
                                // Continue loop
                            }
                            ConsentChoice.NONE -> {
                                // User closed dialog without making a choice
                                println("Bright Data: User closed without making a choice")
                                showConsentRequiredDialog = true
                                isWaitingForConsent = false
                                return@launch
                            }
                        }
                    }

                    // Timeout reached - never got OPTED_IN
                    if (!foundOptedIn) {
                        currentChoice = brightDataSdk.getConsentChoice()
                        println("Bright Data: Timeout after ${maxAttempts}s, final choice: $currentChoice")

                        if (currentChoice == ConsentChoice.OPTED_OUT) {
                            println("Bright Data: User likely declined (stayed OPTED_OUT)")
                        } else {
                            println("Bright Data: No consent given")
                        }

                        // Don't let them play - show required dialog
                        showConsentRequiredDialog = true
                        isWaitingForConsent = false
                    }
                }
            }
        }
    }

    val gameViewModel = viewModel { GameViewModel(repository) }

    AppTheme {
        Surface {
            when (currentScreen) {
                AppScreen.Landing -> {
                    LandingScreen(
                        onPlayClicked = {
                            // Check if user has already opted in
                            val currentChoice = brightDataSdk.getConsentChoice()
                            println("Bright Data: Play clicked - current choice: $currentChoice")

                            if (currentChoice == ConsentChoice.OPTED_IN) {
                                // Already opted in, go directly to game
                                println("Bright Data: Already opted in, navigating to game")
                                currentScreen = AppScreen.Game
                            } else {
                                // Show consent dialog
                                println("Bright Data: Not opted in, showing consent dialog")
                                isWaitingForConsent = true
                                brightDataSdk.showConsentDialog()
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

        // Consent Required Dialog
        if (showConsentRequiredDialog) {
            ConsentRequiredDialog(
                onAcceptClicked = {
                    showConsentRequiredDialog = false
                    isWaitingForConsent = true
                    brightDataSdk.showConsentDialog()
                },
                onDismiss = {
                    showConsentRequiredDialog = false
                    // Stay on landing screen
                }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                brightDataSdk = brightDataSdk,
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
