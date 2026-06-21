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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun DialogActionButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, androidx.compose.ui.Alignment.CenterHorizontally),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.filledTonalButtonColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "取消", modifier = Modifier.size(32.dp))
        }
        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = "确定", modifier = Modifier.size(36.dp))
        }
    }
}
