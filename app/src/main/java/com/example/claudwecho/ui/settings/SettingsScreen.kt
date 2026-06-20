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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Build
import androidx.wear.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToLogin: () -> Unit
) {
    val screenShape by viewModel.screenShape.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(72.dp))
            }
            
            item {
                Button(
                    onClick = { viewModel.toggleScreenShape() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { Text("屏幕形状: $shapeText") },
                    icon = { Icon(Icons.Rounded.Build, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }

            item {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { Text("清除缓存") },
                    secondaryLabel = { Text(cacheSize) },
                    icon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }

            item {
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { Text("退出登录") },
                    icon = { Icon(Icons.AutoMirrored.Rounded.ExitToApp, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
        }
        com.example.claudwecho.ui.components.PinnedHeader(title = "设置")
    }

    if (showConfirm) {
        Dialog(
            onDismissRequest = { showConfirm = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "确认清除缓存？",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { showConfirm = false },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Close, null)
                    }
                    Button(
                        onClick = {
                            viewModel.clearCache()
                            showConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Check, null)
                    }
                }
            }
        }
    }
}
