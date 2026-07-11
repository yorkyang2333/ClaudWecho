package com.yorkyang2333.claudwecho.ui.songinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.data.api.SongDetail
import com.yorkyang2333.claudwecho.ui.components.Button
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun SongInfoScreen(
    songId: Long,
    viewModel: SongInfoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(songId) {
        viewModel.load(songId)
    }

    when (val state = uiState) {
        SongInfoUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is SongInfoUiState.Content -> SongInfoContent(state.song)

        SongInfoUiState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("未能获取歌曲信息", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.load(songId) },
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = "重试",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SongInfoContent(song: SongDetail) {
    val fields = buildList {
        add("歌手" to song.ar.joinToString(" / ") { it.name })
        song.al?.name?.takeIf { it.isNotBlank() }?.let { add("专辑" to it) }
        song.alia.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
            ?.let { add("别名" to it.joinToString(" / ")) }
        song.dt?.takeIf { it > 0 }?.let { add("时长" to formatDuration(it)) }
        song.publishTime?.takeIf { it > 0 }?.let { add("发行日期" to formatDate(it)) }
        val discTrack = listOfNotNull(
            song.cd?.takeIf { it.isNotBlank() }?.let { "碟片 $it" },
            song.no?.takeIf { it > 0 }?.let { "曲目 $it" }
        ).joinToString(" · ")
        discTrack.takeIf { it.isNotBlank() }?.let { add("曲目编号" to it) }
        song.mv?.takeIf { it > 0 }?.let { add("MV" to "可用") }
        add("歌曲 ID" to song.id.toString())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RotaryScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
            item {
                Text(
                    text = song.name,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            items(fields.size) { index ->
                SongInfoField(label = fields[index].first, value = fields[index].second)
            }
        }
        PinnedHeader(title = "歌曲信息")
    }
}

@Composable
private fun SongInfoField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1_000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
