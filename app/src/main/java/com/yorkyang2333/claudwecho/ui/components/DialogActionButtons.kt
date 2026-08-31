package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme

import androidx.compose.ui.unit.Dp

@Composable
fun DialogActionButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelButtonSize: Dp = 48.dp,
    confirmButtonSize: Dp = 56.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp, androidx.compose.ui.Alignment.CenterHorizontally),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.filledTonalButtonColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.size(cancelButtonSize)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "取消", modifier = Modifier.size(cancelButtonSize * 0.55f))
        }
        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = CircleShape,
            modifier = Modifier.size(confirmButtonSize)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = "确定", modifier = Modifier.size(confirmButtonSize * 0.55f))
        }
    }
}
