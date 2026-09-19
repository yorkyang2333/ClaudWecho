package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Standard Wear OS Material 3 list header component.
 * Should be placed as the first item in a list:
 * item {
 *     WearListHeader(title = "Title")
 * }
 */
@Composable
fun WearListHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionIcon: @Composable (() -> Unit)? = null
) {
    ListHeader(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )
            if (actionIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                actionIcon()
            }
        }
    }
}

/**
 * Compatibility helper for existing callers.
 */
@Composable
fun PinnedHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionIcon: @Composable (() -> Unit)? = null
) {
    WearListHeader(
        title = title,
        modifier = modifier,
        actionIcon = actionIcon
    )
}
