package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.dialog.Dialog

/**
 * Standard Wear OS Dialog wrapper.
 * Uses Wear OS dialog container with Material 3 Expressive inner styling.
 */
@Composable
fun WearDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Dialog(
        showDialog = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        content()
    }
}

