package com.alexjlockwood.twentyfortyeight.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk
import com.alexjlockwood.twentyfortyeight.brightsdk.ConsentChoice
import compose_multiplatform_2048.composeapp.generated.resources.Res
import compose_multiplatform_2048.composeapp.generated.resources.brightdata_qr
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
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEDC967),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Full Access Required",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To enjoy unlimited gameplay, please enable full access. This helps us keep the game free for everyone.",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFBBAA99).copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

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
                Text(
                    text = "Disable Full Access?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Disabling web indexing will bring back limited access. Would you like to keep it enabled for full access?",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFBBAA99).copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

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
    currentChoice: ConsentChoice,
    onEnableRequested: () -> Unit,
    onDisableConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDisableDialog by remember { mutableStateOf(false) }
    val isEnabled = currentChoice == ConsentChoice.OPTED_IN
    val learnMoreUrl = "https://bright-sdk.com/users#learn-more-about-bright-sdk-web-indexing"

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
                Image(
                    painter = painterResource(Res.drawable.game_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(80.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3a3a3a)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Web Indexing",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEEEEEE),
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        onEnableRequested()
                                    } else {
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "When enabled, got access to all features!",
                            fontSize = 13.sp,
                            color = Color(0xFFBBAA99).copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Scan the QR Code to Learn More:",
                    fontSize = 13.sp,
                    color = Color(0xFFBBAA99).copy(alpha = 0.7f),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Image(
                    painter = painterResource(Res.drawable.brightdata_qr),
                    contentDescription = "BrightData learn more QR code",
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = learnMoreUrl,
                    fontSize = 13.sp,
                    color = Color(0xFFEEEEEE),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

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

    if (showDisableDialog) {
        DisableAccessDialog(
            onKeepEnabled = {
                showDisableDialog = false
            },
            onDisable = {
                onDisableConfirmed()
                showDisableDialog = false
            }
        )
    }

}





