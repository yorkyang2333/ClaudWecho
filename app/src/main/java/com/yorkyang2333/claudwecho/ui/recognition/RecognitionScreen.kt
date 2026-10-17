package com.yorkyang2333.claudwecho.ui.recognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.data.api.Song
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import com.yorkyang2333.claudwecho.ui.components.SharedSongItem
import com.yorkyang2333.claudwecho.ui.components.SongMenuDialog
import com.yorkyang2333.claudwecho.ui.components.WearListHeader
import com.yorkyang2333.claudwecho.ui.main.FeatureButton

@Composable
fun RecognitionScreen(
    viewModel: RecognitionViewModel,
    onPausePlayback: () -> Unit,
    onPlay: (List<Song>, Int) -> Unit,
    onPlayNext: (Song) -> Unit,
    onSongInfo: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateFlow.collectAsState()
    var pendingStart by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    var permissionBlocked by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingStart = true
            permissionMessage = null
            permissionBlocked = false
        } else {
            permissionMessage = "需要麦克风权限才能听歌识曲"
            permissionBlocked = (context as? Activity)?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.RECORD_AUDIO)
            } ?: false
        }
    }
    LaunchedEffect(pendingStart, lifecycleState) {
        if (pendingStart && lifecycleState == Lifecycle.State.RESUMED) {
            pendingStart = false
            viewModel.start(onPausePlayback)
        }
    }
    DisposableEffect(viewModel, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.cancel()
            if (event == Lifecycle.Event.ON_STOP) pendingStart = false
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            pendingStart = false
            viewModel.cancel()
        }
    }

    RotaryScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { WearListHeader(title = "听歌识曲") }
        item {
            Text(
                text = permissionMessage ?: when (state.stage) {
                    RecognitionStage.IDLE -> "聆听身边的音乐\n采集 3 秒后自动识别"
                    RecognitionStage.RECORDING -> "正在聆听… ${kotlin.math.ceil((1f - state.progress) * 3).toInt().coerceAtLeast(1)} 秒"
                    RecognitionStage.FINGERPRINTING -> "正在分析音频…"
                    RecognitionStage.QUERYING -> "正在识别歌曲…"
                    RecognitionStage.RESULT -> if (state.songs.isEmpty()) "没有识别到歌曲\n请靠近音源后重试" else "识别到以下歌曲"
                    RecognitionStage.ERROR -> state.message ?: "识别失败，请重试"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.isBusy) {
            item { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
            item { FeatureButton(Icons.Rounded.Close, "取消识别") { viewModel.cancel() } }
        } else {
            if (permissionBlocked) {
                item {
                    FeatureButton(Icons.Rounded.Settings, "允许麦克风权限") {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()))
                        permissionBlocked = false
                        permissionMessage = null
                    }
                }
            }
            items(state.songs.size, key = { state.songs[it].id }) { index ->
                val song = state.songs[index]
                SharedSongItem(song = song, onClick = { onPlay(state.songs, index) }, onLongClick = { selectedSong = song })
            }
            item {
                FeatureButton(
                    if (state.stage == RecognitionStage.IDLE) Icons.Rounded.Mic else Icons.Rounded.Refresh,
                    if (state.stage == RecognitionStage.IDLE) "开始识别" else "重新识别"
                ) {
                    permissionMessage = null
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        permissionBlocked = false
                        pendingStart = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        }
    }
    SongMenuDialog(
        showDialog = selectedSong != null,
        song = selectedSong,
        onDismissRequest = { selectedSong = null },
        onPlayNext = { selectedSong?.let(onPlayNext) },
        onSongInfo = onSongInfo
    )
}
