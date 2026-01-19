package com.alexjlockwood.twentyfortyeight.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose_multiplatform_2048.composeapp.generated.resources.Res
import compose_multiplatform_2048.composeapp.generated.resources.game_logo
import org.jetbrains.compose.resources.painterResource

/**
 * Landing screen for the 2048 game with minimal, soft, and attractive UI.
 * Shows Play and Settings buttons.
 */
@Composable
fun LandingScreen(
    onPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Subtle floating animation for numbers
    val infiniteTransition = rememberInfiniteTransition()
    val float1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val float2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Gentle pulse for Play button
    val playButtonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Fade in animations
    var isVisible by remember { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a)),
        contentAlignment = Alignment.Center
    ) {
        // Floating decorative numbers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.05f)
        ) {
            Text(
                text = "2",
                fontSize = 180.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBBAA99),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-20).dp, y = float1Offset.dp)
            )
            Text(
                text = "4",
                fontSize = 140.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEDC967),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = float2Offset.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(48.dp)
                .alpha(fadeAlpha)
                .offset(y = slideOffset.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(Res.drawable.game_logo),
                contentDescription = "2048 Game Logo",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Slide to combine numbers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFBBAA99).copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Play Button - Soft and minimal
            Button(
                onClick = onPlayClicked,
                modifier = Modifier
                    .scale(playButtonScale)
                    .width(240.dp)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDC967),
                    contentColor = Color(0xFF1a1a1a)
                ),
                shape = RoundedCornerShape(32.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Play",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Button - Minimal text button
            TextButton(
                onClick = onSettingsClicked,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFBBAA99).copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFBBAA99).copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Minimal footer
            Text(
                text = "Get to the 2048 tile to win!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFBBAA99).copy(alpha = 0.4f)
            )
        }
    }
}
