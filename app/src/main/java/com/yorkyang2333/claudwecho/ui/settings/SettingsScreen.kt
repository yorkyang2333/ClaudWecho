package com.yorkyang2333.claudwecho.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
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
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.WbSunny
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
    val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf(apiBaseUrl) }

    val shapeText = when (screenShape) {
        "round" -> "圆屏"
        "square" -> "方屏"
        else -> "自动检测"
    }

    val urlLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val results = android.app.RemoteInput.getResultsFromIntent(data)
                val url = results?.getCharSequence("api_url")?.toString()
                if (!url.isNullOrBlank()) {
                    viewModel.setApiBaseUrl(url)
                    android.widget.Toast.makeText(viewModel.getApplicationContext(), "后端地址已修改，重启应用后生效", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RotaryScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(48.dp))
            }
            
            item {
                Button(
                    onClick = {
                        val intent = androidx.wear.input.RemoteInputIntentHelper.createActionRemoteInputIntent()
                        val remoteInputs = listOf(
                            android.app.RemoteInput.Builder("api_url")
                                .setLabel("输入后端地址")
                                .build()
                        )
                        androidx.wear.input.RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                        try {
                            urlLauncher.launch(intent)
                        } catch (e: Exception) {
                            showUrlDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = "后端地址",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    secondaryLabel = { 
                        Text(
                            text = apiBaseUrl,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    icon = { Icon(Icons.Rounded.Link, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            
            item {
                Button(
                    onClick = { viewModel.toggleScreenShape() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = "屏幕形状",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    secondaryLabel = { Text(shapeText) },
                    icon = { Icon(Icons.Rounded.Build, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            item {
                Button(
                    onClick = { viewModel.toggleKeepScreenOn() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = "屏幕常亮",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    secondaryLabel = { Text(if (keepScreenOn) "开启" else "关闭") },
                    icon = { Icon(Icons.Rounded.WbSunny, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            item {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = "清除缓存",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    secondaryLabel = { Text(cacheSize) },
                    icon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
        }
        com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "设置")
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

    if (showUrlDialog) {
        Dialog(
            onDismissRequest = { showUrlDialog = false }
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
                    text = "输入后端地址",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                androidx.compose.foundation.text.BasicTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = androidx.compose.ui.graphics.Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(androidx.compose.ui.graphics.Color.DarkGray)
                        .padding(8.dp)
                )
                com.yorkyang2333.claudwecho.ui.components.DialogActionButtons(
                    onCancel = { showUrlDialog = false },
                    onConfirm = {
                        viewModel.setApiBaseUrl(tempUrl)
                        android.widget.Toast.makeText(viewModel.getApplicationContext(), "后端地址已修改，重启应用后生效", android.widget.Toast.LENGTH_SHORT).show()
                        showUrlDialog = false
                    }
                )
            }
        }
    }
}
