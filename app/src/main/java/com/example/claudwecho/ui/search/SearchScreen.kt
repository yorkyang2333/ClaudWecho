package com.example.claudwecho.ui.search

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import com.example.claudwecho.ui.components.SharedSongItem
import com.example.claudwecho.ui.player.PlayerViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    playerViewModel: PlayerViewModel,
    onSongClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val listState = rememberScalingLazyListState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val results = RemoteInput.getResultsFromIntent(data)
                val query = results?.getCharSequence("search_query")?.toString()
                if (!query.isNullOrBlank()) {
                    viewModel.performSearch(query)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (error != null) {
            item {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (searchResults.isNotEmpty()) {
            items(searchResults) { song ->
                SharedSongItem(
                    song = song,
                    onClick = {
                        playerViewModel.playPlaylist(searchResults, searchResults.indexOf(song))
                        onSongClick()
                    }
                )
            }
        }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Button(
                onClick = {
                    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                    val remoteInputs = listOf(
                        RemoteInput.Builder("search_query")
                            .setLabel("搜索歌曲、歌手或专辑")
                            .build()
                    )
                    RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                    launcher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "搜索",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (searchQuery.isNotEmpty()) searchQuery else "点击搜索",
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
