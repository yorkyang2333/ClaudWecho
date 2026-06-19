package com.example.claudwecho.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val screenShape by viewModel.screenShape.collectAsState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item {
            Text(
                text = "屏幕形状",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        val options = listOf(
            "auto" to "自动检测",
            "round" to "圆屏",
            "square" to "方屏"
        )
        
        items(options.size) { index ->
            val (key, label) = options[index]
            val isSelected = screenShape == key
            Button(
                onClick = { viewModel.setScreenShape(key) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = {
                    Text(text = label, style = MaterialTheme.typography.labelMedium)
                },
                secondaryLabel = {
                    if (isSelected) {
                        Text(text = "已选", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}
