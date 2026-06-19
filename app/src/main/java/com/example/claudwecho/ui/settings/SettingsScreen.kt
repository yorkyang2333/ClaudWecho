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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.wear.compose.material3.Icon

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToLogin: () -> Unit
) {
    val screenShape by viewModel.screenShape.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            onNavigateToLogin()
        }
    }

    val shapeText = when (screenShape) {
        "round" -> "圆屏"
        "square" -> "方屏"
        else -> "自动检测"
    }

    ScalingLazyColumn(
        autoCentering = null,
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
            Button(
                onClick = { viewModel.toggleScreenShape() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text("屏幕形状: $shapeText") },
                icon = { Icon(Icons.Filled.Build, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }

        item {
            Button(
                onClick = { viewModel.clearCache() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text("清除缓存") },
                secondaryLabel = { Text(cacheSize) },
                icon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }

        item {
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text("退出登录") },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }
    }
}
