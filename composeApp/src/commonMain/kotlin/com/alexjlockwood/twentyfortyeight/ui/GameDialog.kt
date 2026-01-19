package com.alexjlockwood.twentyfortyeight.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameDialog(
    title: String,
    message: String,
    onConfirmListener: () -> Unit,
    onDismissListener: (() -> Unit)?,
    onQuitListener: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // TODO: update this to match iOS style dialog on iOS
    AlertDialog(
        modifier = modifier,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onConfirmListener() }) { Text("OK") }
                onQuitListener?.let { quitListener ->
                    TextButton(onClick = { quitListener() }) { Text("Quit") }
                }
            }
        },
        dismissButton = onDismissListener?.let { { TextButton(onClick = it) { Text("Cancel") } } },
        onDismissRequest = { onDismissListener?.invoke() },
    )
}
