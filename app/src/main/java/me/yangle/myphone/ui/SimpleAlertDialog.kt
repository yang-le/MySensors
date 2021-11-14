package me.yangle.myphone.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun SimpleAlertDialog(
    title: String? = null,
    text: String? = null,
    confirmText: String = "Confirm",
    dismissText: String = "Dismiss",
    dismissContent: @Composable (() -> Unit)? = null,
    onConfirm: (() -> Unit)
) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { dismissContent?.let { openDialog.value = false } },
            title = title?.let { { Text(title) } },
            text = text?.let { { Text(text) } },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            },
            dismissButton = dismissContent?.let {
                {
                    TextButton(onClick = { openDialog.value = false }
                    ) {
                        Text(dismissText)
                    }
                }
            }
        )
    } else {
        dismissContent?.invoke()
    }
}
