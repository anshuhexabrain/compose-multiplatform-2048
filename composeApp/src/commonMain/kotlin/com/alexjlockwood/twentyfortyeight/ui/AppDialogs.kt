package com.alexjlockwood.twentyfortyeight.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk
import com.alexjlockwood.twentyfortyeight.brightsdk.ConsentChoice
import compose_multiplatform_2048.composeapp.generated.resources.Res
import compose_multiplatform_2048.composeapp.generated.resources.game_logo
import org.jetbrains.compose.resources.painterResource

/**
 * Dialog shown when user tries to play without accepting consent.
 */
@Composable
fun ConsentRequiredDialog(
    onAcceptClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEDC967),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Full Access Required",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = "To enjoy unlimited gameplay, please enable full access. This helps us keep the game free for everyone.",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFBBAA99).copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFBBAA99)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBAA99).copy(alpha = 0.5f))
                    ) {
                        Text("Not Now", fontSize = 15.sp)
                    }

                    Button(
                        onClick = onAcceptClicked,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEDC967),
                            contentColor = Color(0xFF1a1a1a)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Enable", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown when user tries to disable full access.
 */
@Composable
fun DisableAccessDialog(
    onKeepEnabled: () -> Unit,
    onDisable: () -> Unit
) {
    Dialog(onDismissRequest = onKeepEnabled) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Disable Full Access?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = "Disabling web indexing will bring back limited access. Would you like to keep it enabled for full access?",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFBBAA99).copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDisable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.5f))
                    ) {
                        Text("No, Disable", fontSize = 15.sp)
                    }

                    Button(
                        onClick = onKeepEnabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEDC967),
                            contentColor = Color(0xFF1a1a1a)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Yes, Keep", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Settings dialog with toggle switch to manage full access.
 */
@Composable
fun SettingsDialog(
    brightDataSdk: BrightDataSdk,
    onDismiss: () -> Unit
) {
    var currentChoice by remember { mutableStateOf(brightDataSdk.getConsentChoice()) }
    var showDisableDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val isEnabled = currentChoice == ConsentChoice.OPTED_IN

    // Update current choice when dialog is shown
    LaunchedEffect(Unit) {
        currentChoice = brightDataSdk.getConsentChoice()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo at top
                Image(
                    painter = painterResource(Res.drawable.game_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Full Access Toggle Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3a3a3a)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Full Access",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEEEEEE)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isEnabled) "Active" else "Disabled",
                                    fontSize = 13.sp,
                                    color = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFBBAA99).copy(alpha = 0.5f)
                                )
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        // User wants to enable - show consent dialog
                                        brightDataSdk.showConsentDialog()
                                    } else {
                                        // User wants to disable - show confirmation dialog
                                        showDisableDialog = true
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFEDC967),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFBBAA99).copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Enable this to enjoy unlimited gameplay",
                            fontSize = 13.sp,
                            color = Color(0xFFBBAA99).copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Learn More Section
                OutlinedButton(
                    onClick = {
                        uriHandler.openUri("https://bright-sdk.com/users#learn-more-about-bright-sdk-web-indexing")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFBBAA99)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBAA99).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Learn More About Web Indexing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEDC967),
                        contentColor = Color(0xFF1a1a1a)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Close", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Show disable confirmation dialog
    if (showDisableDialog) {
        DisableAccessDialog(
            onKeepEnabled = {
                showDisableDialog = false
            },
            onDisable = {
                brightDataSdk.optOut()
                currentChoice = ConsentChoice.OPTED_OUT
                showDisableDialog = false
            }
        )
    }

    // Listen for consent changes
    LaunchedEffect(Unit) {
        brightDataSdk.setChoiceChangeCallback { choice ->
            currentChoice = choice
        }
    }
}
