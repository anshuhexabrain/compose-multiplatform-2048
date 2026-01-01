package com.alexjlockwood.twentyfortyeight.brightsdk

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable UI for Bright Data SDK settings.
 * Shows the current consent status and allows users to opt-in or opt-out.
 *
 * @param sdk The BrightDataSdk instance
 * @param modifier Modifier for the container
 */
@Composable
fun BrightDataSettings(
    sdk: BrightDataSdk,
    modifier: Modifier = Modifier
) {
    var currentChoice by remember { mutableStateOf(sdk.getConsentChoice()) }

    // Update choice when it changes via callback
    LaunchedEffect(Unit) {
        sdk.setChoiceChangeCallback { newChoice ->
            currentChoice = newChoice
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bright Data Settings",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = when (currentChoice) {
                    ConsentChoice.OPTED_IN -> "Status: Opted In - Thank you for your support!"
                    ConsentChoice.OPTED_OUT -> "Status: Opted Out"
                    ConsentChoice.NONE -> "Status: No choice made yet"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (currentChoice) {
                    ConsentChoice.OPTED_IN -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = "Share your idle network resources to support free features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        sdk.showConsentDialog()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when (currentChoice) {
                            ConsentChoice.NONE -> "Get Started"
                            else -> "Change Settings"
                        }
                    )
                }

                if (currentChoice == ConsentChoice.OPTED_IN) {
                    OutlinedButton(
                        onClick = {
                            sdk.optOut()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Opt Out")
                    }
                }
            }
        }
    }
}

/**
 * Compact version for showing in the game UI.
 */
@Composable
fun BrightDataStatusBadge(
    sdk: BrightDataSdk,
    modifier: Modifier = Modifier
) {
    var currentChoice by remember { mutableStateOf(sdk.getConsentChoice()) }

    LaunchedEffect(Unit) {
        sdk.setChoiceChangeCallback { newChoice ->
            currentChoice = newChoice
        }
    }

    if (currentChoice == ConsentChoice.NONE && sdk.isSupported()) {
        TextButton(
            onClick = { sdk.showConsentDialog() },
            modifier = modifier
        ) {
            Text("☆ Support this app - Click here")
        }
    }
}
