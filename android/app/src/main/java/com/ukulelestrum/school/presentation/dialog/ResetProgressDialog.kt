package com.ukulelestrum.school.presentation.dialog

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ukulelestrum.school.R

@Composable
fun ResetProgressDialog(resetting: Boolean, error: Boolean, onDismiss: () -> Unit, onReset: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reset_title)) },
        text = { Text(stringResource(if (error) R.string.reset_failed else R.string.reset_message)) },
        confirmButton = { TextButton(onReset, Modifier.heightIn(min = 48.dp), enabled = !resetting) { Text(stringResource(if (error) R.string.retry else R.string.reset)) } },
        dismissButton = { TextButton(onDismiss, Modifier.heightIn(min = 48.dp), enabled = !resetting) { Text(stringResource(R.string.cancel)) } })
}
