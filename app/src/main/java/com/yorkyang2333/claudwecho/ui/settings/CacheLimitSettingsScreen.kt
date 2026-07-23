package com.yorkyang2333.claudwecho.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.Button
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn

private data class CacheOption(val limitMb: Int, val labelText: String)

@Composable
fun CacheLimitSettingsScreen(
    viewModel: SettingsViewModel,
    onOptionSelected: () -> Unit = {}
) {
    val currentLimitMb by viewModel.audioCacheLimitMb.collectAsState()

    val options = listOf(
        CacheOption(200, "200 MB"),
        CacheOption(500, "500 MB"),
        CacheOption(1000, "1 GB"),
        CacheOption(2000, "2 GB")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        RotaryScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }

            items(options.size) { index ->
                val option = options[index]
                val isSelected = option.limitMb == currentLimitMb
                Button(
                    onClick = {
                        viewModel.setAudioCacheLimit(option.limitMb)
                        onOptionSelected()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = {
                        Text(
                            text = option.labelText,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = if (isSelected) {
                        { Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) }
                    } else null
                )
            }
        }
        PinnedHeader(title = "缓存上限")
    }
}
