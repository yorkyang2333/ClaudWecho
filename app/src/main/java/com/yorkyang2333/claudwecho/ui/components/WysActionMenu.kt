package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

data class WysActionMenuItem(
    val label: String,
    val icon: ImageVector,
    val destructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun WysActionMenu(
    show: Boolean,
    title: String,
    items: List<WysActionMenuItem>,
    onDismissRequest: () -> Unit
) {
    Dialog(
        showDialog = show,
        onDismissRequest = onDismissRequest
    ) {
        val listState = rememberScalingLazyListState()

        RotaryScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = rotaryContentPadding()
        ) {
            item {
                WearListHeader(title = title)
            }
            items(items, key = { it.label }) { action ->
                Button(
                    onClick = {
                        onDismissRequest()
                        action.onClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null
                        )
                    },
                    colors = if (action.destructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            iconColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            iconColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                ) {
                    Text(action.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
